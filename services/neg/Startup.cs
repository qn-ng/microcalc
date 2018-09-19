using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.HttpsPolicy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace app
{
    public class Startup
    {
        public static readonly string APP_VERSION = "v1";
        public static readonly string APP_SERVICE = "name: neg, version: " + APP_VERSION;
        public static string MULT_URI;
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
            MULT_URI = configuration.GetValue<string>("MULT_ENDPOINT", "http://mult/api");
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddMvc().SetCompatibilityVersion(CompatibilityVersion.Version_2_1);
            services.Configure<ApiBehaviorOptions>(options =>
                {
                    options.InvalidModelStateResponseFactory = actionContext =>
                    {
                        var errors = actionContext.ModelState
                            .Where(e => e.Value.Errors.Count > 0)
                            .Select(e => e.Value.Errors.First().ErrorMessage)
                            .ToArray();

                        return new BadRequestObjectResult(new
                        {
                            error = errors,
                            services = APP_SERVICE
                        });
                    };
                });
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IHostingEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                app.UseHsts();
            }

            app.UseMvc();
        }
    }
}
