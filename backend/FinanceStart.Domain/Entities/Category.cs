namespace FinanceStart.Domain.Entities;

public class Category
{
    public Guid Id {get; set;}
    public Guid UserId {get; set;}
    public string Name {get; set;} = string.Empty;
    public TransactionType Type {get; set;}
    public string Color {get; set;} = "#6750A4";
    public string? Icon {get; set;}
    public DateTime CreatedAt {get; set;}
}