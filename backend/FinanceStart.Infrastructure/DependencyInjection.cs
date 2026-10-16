using FinanceStart.Domain.Entities;
using FinanceStart.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace FinanceStart.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services,  string connectionString)
    {
        services.AddDbContext<FinanceDbContext>(options =>
        options
        .UseNpgsql(connectionString, npgsql =>
        npgsql.MapEnum<TransactionType>("transaction_type"))
        .UseSnakeCaseNamingConvention());

        return services;
    }
}