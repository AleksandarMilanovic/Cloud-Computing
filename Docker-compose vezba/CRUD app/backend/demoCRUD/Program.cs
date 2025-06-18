using demoCRUD.Data;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using System.Threading;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseMySql(
        builder.Configuration.GetConnectionString("DefaultConnection"),
        ServerVersion.AutoDetect(builder.Configuration.GetConnectionString("DefaultConnection")))
    );

var MyAllowSpecificOrigins = "_myAllowSpecificOrigins";

builder.Services.AddCors(options =>
{
    options.AddPolicy(name: MyAllowSpecificOrigins,
        policy =>
        {
            policy.WithOrigins(
                "http://localhost:3000",      // Dodato za React frontend
                "http://localhost:5143",
                "http://192.168.1.6:5143",
                "http://192.168.1.6:8081"
            )
            .AllowAnyHeader()
            .AllowAnyMethod();
        });
});

var app = builder.Build();

// Retry migracija baze podataka - korisno ako MySQL kasni sa startom
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();

    var retryCount = 0;
    var maxRetries = 10;

    while (retryCount < maxRetries)
    {
        try
        {
            db.Database.Migrate();
            break; // uspešno izvršena migracija
        }
        catch (Exception ex)
        {
            retryCount++;
            Console.WriteLine($"[Migration Retry {retryCount}] Waiting for DB... {ex.Message}");
            Thread.Sleep(3000); // Čekaj 3 sekunde pre ponovnog pokušaja
        }
    }
}

app.UseCors(MyAllowSpecificOrigins);
app.UseRouting();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();
