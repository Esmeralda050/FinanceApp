using System.Security.Cryptography.X509Certificates;
using FinanceStart.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace FinanceStart.Infrastructure.Persistence;

public class FinanceDbContext(DbContextOptions<FinanceDbContext> options ) : DbContext(options)
{
    //Cada DbSet es una tabla
    public DbSet<Category> Categories => Set<Category>();
    public DbSet<Transaction> Transactions => Set<Transaction>();
    public DbSet<Budget> Budgets => Set<Budget>();


    //Ajustes de como se mapean las clases en las tablas
    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.HasPostgresEnum<TransactionType>("public", "transaction_type");

        modelBuilder.Entity<Category>(categoryTable =>
        {
            categoryTable.Property(category  => category .CreatedAt).HasDefaultValueSql("now()");
        });

        modelBuilder.Entity<Transaction>(transactionTable  =>
        {
            transactionTable .Property(transaction  => transaction .Amount).HasPrecision(14, 2);
            transactionTable .Property(transaction  => transaction .CreatedAt).HasDefaultValueSql("now()");
            transactionTable .Property(transaction  => transaction.UpdatedAt).HasDefaultValueSql("now()");
            
            transactionTable
            .HasOne(transaction => transaction.Category)
            .WithMany()
            .HasForeignKey(transaction => transaction.CategoryId);
        });

        modelBuilder.Entity<Budget>(budgetTable  =>
        {
            budgetTable .Property(budget  => budget .Amount).HasPrecision(14, 2);
            budgetTable .Property(budget  => budget .CreatedAt).HasDefaultValueSql("now()");
            
            budgetTable
            .HasOne(budget  => budget .Category)
            .WithMany()
            .HasForeignKey(budget  => budget .CategoryId);
        });
    }
}