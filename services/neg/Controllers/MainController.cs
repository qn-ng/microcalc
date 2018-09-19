using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;
using System.Net.Http;
using Newtonsoft.Json;
using System.Text;

namespace app.Controllers
{
    [ApiRoute("")]
    [Consumes("application/json")]
    [Produces("application/json")]
    [ApiController]
    public class MainController : ControllerBase
    {
        private static readonly List<string> TRACING_HEADERS = new List<string>{
            "x-request-id",
            "x-b3-traceid",
            "x-b3-spanid",
            "x-b3-parentspanid",
            "x-b3-sampled",
            "x-b3-flags",
            "x-ot-span-context"
        };

        private static readonly HttpClient httpClient = new HttpClient();

        // GET api/values
        [HttpGet("status")]
        public IActionResult GetStatus()
        {
            return Ok(new { data = "OK" });
        }

        // POST api/values
        [HttpPost("neg")]
        public async Task<IActionResult> Post([FromBody] Request req)
        {
            try
            {
                var headers = TRACING_HEADERS
                    .Where(header => Request.Headers.ContainsKey(header))
                    .Select(header => KeyValuePair.Create(header, Request.Headers[header].DefaultIfEmpty(null)))
                    .Where(p => p.Value != null);

                HttpResponseMessage response;
                using (var requestMessage = new HttpRequestMessage(HttpMethod.Post, Startup.MULT_URI))
                {
                    foreach (var header in headers)
                    {
                        requestMessage.Headers.Add(header.Key, header.Value);
                    }
                    requestMessage.Content = new JsonContent(new { operands = new int[] { req.Operands[0], -1 } });
                    response = await httpClient.SendAsync(requestMessage);
                }

                response.EnsureSuccessStatusCode();
                MultResponse responseBody = await response.Content.ReadAsAsync<MultResponse>();

                return Ok(new MultResponse
                {
                    Result = responseBody.Result,
                    Operands = req.Operands,
                    Service = Startup.APP_SERVICE,
                    Origins = new MultResponse[] { responseBody }
                });
            }
            catch (HttpRequestException e)
            {
                Console.WriteLine("\nException Caught!");
                Console.WriteLine("Message :{0} ", e.Message);
                return BadRequest(new
                {
                    error = e.Message,
                    service = Startup.APP_SERVICE
                });
            }
        }
    }

    public class MultResponse
    {
        [Required]
        public int Result { get; set; }
        public int[] Operands { get; set; }
        public string Service { get; set; }
        public MultResponse[] Origins { get; set; }
    }

    public class Request
    {
        [Required]
        [MaxLength(1)]
        [MinLength(1)]
        public int[] Operands { get; set; }
    }

    public class ApiRoute : RouteAttribute
    {
        public ApiRoute(string route) : base("api/" + Startup.APP_VERSION + "/" + route) { }
    }

    public class JsonContent : StringContent
    {
        public JsonContent(object value)
            : base(JsonConvert.SerializeObject(value), Encoding.UTF8,
            "application/json")
        {
        }

        public JsonContent(object value, string mediaType)
            : base(JsonConvert.SerializeObject(value), Encoding.UTF8, mediaType)
        {
        }
    }
}
