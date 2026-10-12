-- Extensiones
create extension if not exists "pgcrypto";

-- Tipos
create type transaction_type as enum ('income', 'expense');

-- Perfiles (1:1 con auth.users)
create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  currency char(3) not null default 'DOP',
  created_at timestamptz not null default now()
);

-- Categorias
create table public.categories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  name text not null check (char_length(name) between 1 and 50),
  type transaction_type not null,
  color char(7) not null default '#6750A4',
  icon text,
  created_at timestamptz not null default now(),
  unique (user_id, name, type)
);

-- Transacciones
create table public.transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  category_id uuid references public.categories(id) on delete set null,
  type transaction_type not null,
  amount numeric(14,2) not null check (amount > 0),
  description text,
  occurred_at date not null default current_date,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_transactions_user_date on public.transactions (user_id, occurred_at desc);
create index idx_transactions_category on public.transactions (category_id);

-- Presupuestos mensuales por categoria
create table public.budgets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  category_id uuid not null references public.categories(id) on delete cascade,
  month date not null check (month = date_trunc('month', month)::date),
  amount numeric(14,2) not null check (amount > 0),
  created_at timestamptz not null default now(),
  unique (user_id, category_id, month)
);

-- updated_at automatico
create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end $$;

create trigger trg_transactions_updated_at
before update on public.transactions
for each row execute function public.set_updated_at();

-- Crear perfil + categorias por defecto al registrarse
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into profiles (id, full_name)
  values (new.id, new.raw_user_meta_data->>'full_name');

  insert into categories (user_id, name, type, color, icon) values
    (new.id, 'Salario',        'income',  '#2E7D32', 'payments'),
    (new.id, 'Otros ingresos', 'income',  '#00897B', 'savings'),
    (new.id, 'Comida',         'expense', '#E65100', 'restaurant'),
    (new.id, 'Transporte',     'expense', '#1565C0', 'directions_car'),
    (new.id, 'Vivienda',       'expense', '#6A1B9A', 'home'),
    (new.id, 'Servicios',      'expense', '#F9A825', 'bolt'),
    (new.id, 'Entretenimiento','expense', '#AD1457', 'movie'),
    (new.id, 'Salud',          'expense', '#C62828', 'favorite');
  return new;
end $$;

create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

-- Validar que la categoria pertenezca al usuario y coincida el tipo
create or replace function public.check_transaction_category()
returns trigger language plpgsql as $$
begin
  if new.category_id is not null and not exists (
    select 1 from categories c
    where c.id = new.category_id
      and c.user_id = new.user_id
      and c.type = new.type
  ) then
    raise exception 'Categoría inválida para esta transacción';
  end if;
  return new;
end $$;

create trigger trg_check_transaction_category
before insert or update on public.transactions
for each row execute function public.check_transaction_category();

-- Row Level Security
alter table public.profiles     enable row level security;
alter table public.categories   enable row level security;
alter table public.transactions enable row level security;
alter table public.budgets      enable row level security;

create policy "profiles_own" on public.profiles
  for all using (auth.uid() = id) with check (auth.uid() = id);

create policy "categories_own" on public.categories
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "transactions_own" on public.transactions
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

create policy "budgets_own" on public.budgets
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);

-- Resumen mensual (respeta RLS)
create or replace view public.monthly_summary
with (security_invoker = true) as
select
  user_id,
  date_trunc('month', occurred_at)::date as month,
  sum(amount) filter (where type = 'income')  as total_income,
  sum(amount) filter (where type = 'expense') as total_expense,
  coalesce(sum(amount) filter (where type = 'income'), 0)
    - coalesce(sum(amount) filter (where type = 'expense'), 0) as balance
from public.transactions
group by user_id, date_trunc('month', occurred_at);

-- Gasto por categoria en el mes (para graficos)
create or replace function public.spending_by_category(p_month date)
returns table (category_id uuid, name text, color text, total numeric, budget numeric)
language sql stable security invoker as $$
  select c.id, c.name, c.color,
         coalesce(sum(t.amount), 0) as total,
         b.amount as budget
  from categories c
  left join transactions t
    on t.category_id = c.id
   and date_trunc('month', t.occurred_at) = date_trunc('month', p_month)
  left join budgets b
    on b.category_id = c.id
   and b.month = date_trunc('month', p_month)::date
  where c.type = 'expense'
  group by c.id, c.name, c.color, b.amount
  order by total desc;
$$;