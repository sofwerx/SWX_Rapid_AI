﻿using System;
using RabbitMQ.Client;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System.Threading.Tasks;
using System.IO;

namespace RabbitMQClient
{
    class Program
    {
        private static string EXCHANGE = "dragonfly";
        private static string ROUTING_KEY = "dragonfly.demo_entities";
        private static string EVENT_ROUTING_KEY = "dragonfly.demo_alerts";
        private static string RABBITMQ_HOSTNAME = "dragonfly.caeusa.com";
        private static string PASSWORD = "dragonfly";
        private static string USERNAME = "rapidx";
        private static bool USE_RAPIDX = true;

        private static string MESSAGE = "{\"uid\":\"CustomIDForTAKServerTest.Entity\",\"type\":\"a-f-G-U-C\",\"time\":\"1614187736429\",\"start\":\"1614187736429\",\"stale\":\"1614191352000\",\"how\":\"m-g\",\"point\":{\"lat\":\"39.0495\",\"lon\":\"-85.7445\",\"hae\":\"9999999\",\"ce\":\"9999999\",\"le\":\"9999999\"},\"detail\":{\"milsym2525C\":\"SFGPUCI*****\", \"video\":\"https://cdn.bitdegree.org/learn/Pexels%20Videos%203373.mp4?raw=true\", \"image\": \"IMAGEPLACEHOLDER\", \"feedType\":\"OSH_SENSOR\",\"serviceDetails\":{\"serviceUrl\":\"THE SERVICE URL\",\"provider\":{\"providerName\":\"SOME NAME\",\"providerData\":\"SOME DATA\"},\"offeringID\":\"THE OFFERING ID\",\"temporalData\":\"THE TEMPORAL DATA\"}}}";

        //private static string EVENT_MESSAGE = "{\"cot_uid\":\"CustomIDForTAKServerTest.Alert\",\"message\":\"This is a demo alert from a test client (no location)\",\"type\":\"info\"}";
                
        private static string EVENT_MESSAGE = "{\"cot_uid\":\"CustomIDForTAKServerTest.Entity\",\"message\":\"This is a demo alert from a test client at TIME\",\"type\":\"info\"}";
        //ANDROID-1e159fc8250eb070
        //private static string EVENT_MESSAGE = "{\"cot_uid\":\"ANDROID-1e159fc8250eb070\",\"message\":\"This is a demo alert from a test client (no location)\",\"type\":\"info\"}";

        private static string EVENT_MESSAGE_WITH_LOCATION = "{\"cot_uid\":\"CustomIDForTAKServerTest.Entity\",\"message\":\"This is a demo alert from a test client\",\"type\":\"info\",\"point\":{\"lat\":\"30.4299511\",\"lon\":\"-86.6061093\",\"hae\":\"9999999\",\"ce\":\"9999999\",\"le\":\"9999999\"}}";

        private static string IMAGE_URL = @"https://i.picsum.photos/id/1025/4951/3301.jpg?hmac=_aGh5AtoOChip_iaMo8ZvvytfEojcgqbCH7dzaz-H8Y";

        private static Random _random = new Random();
        private static double MIN_LAT = 39.04;
        private static double MAX_LAT = 39.05;

        private static double MIN_LON = -85.74;
        private static double MAX_LON = -85.75;
        
        static void Main(string[] args)
        {
            ConnectionFactory factory = new ConnectionFactory();
            
            if (USE_RAPIDX)
            {
                factory.UserName = USERNAME;
                factory.Password = PASSWORD;
                factory.HostName = RABBITMQ_HOSTNAME;
            }
            else 
                factory.HostName = "localhost";

            using var connection = factory.CreateConnection();
            using var channel = connection.CreateModel();
            channel.ExchangeDeclare(EXCHANGE, ExchangeType.Topic, true);

            //Entities
            Task.Run(async () =>
            {
                try
                {
                    while (true)
                    {
                        var message = MassageMessage(MESSAGE);
                        var body = Encoding.UTF8.GetBytes(message);

                        channel.BasicPublish(exchange: EXCHANGE,
                                        routingKey: ROUTING_KEY,
                                        basicProperties: null,
                                        body: body);

                        Console.WriteLine(" [x] Sent {0}", message);
                        await Task.Delay(5000);
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.Message);
                }
            });

            //Events
            Task.Run(async () =>
            {
                try
                {
                    while (true)
                    {
                        var message = EVENT_MESSAGE.Replace("TIME", DateTime.UtcNow.ToString("HH:mm:ss"));
                        var body = Encoding.UTF8.GetBytes(message);

                        channel.BasicPublish(exchange: EXCHANGE,
                                        routingKey: EVENT_ROUTING_KEY,
                                        basicProperties: null,
                                        body: body);

                        Console.WriteLine(" [x] Sent {0}", message);
                        await Task.Delay(20000);
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine(ex.Message);
                }
            });

            Console.ReadLine();
        }

        private static string MassageMessage(string message)
        {
            try
            {
                var imageBase64 = string.Empty;
                using (var sr = new StreamReader("Base64Image.txt"))
                    imageBase64 = sr.ReadToEnd();

                var randomNumber = GetRandomNumber(0, 10);
                if(IsOdd(randomNumber))
                    message = message.Replace("IMAGEPLACEHOLDER", @"data:image/png;base64," + imageBase64);
                else
                    message = message.Replace("IMAGEPLACEHOLDER", IMAGE_URL);

                var jObject = JObject.Parse(message);

                var newLat = GetRandomNumber(MIN_LAT, MAX_LAT);
                var newLon = GetRandomNumber(MIN_LON, MAX_LON);

                jObject["point"]["lat"] = newLat;
                jObject["point"]["lon"] = newLon;

                var currentIsoTime = DateTime.UtcNow.ToString("o");
                var staleIsoTime = (DateTime.UtcNow + TimeSpan.FromMinutes(10)).ToString("o");
                jObject["time"] = currentIsoTime;
                jObject["start"] = currentIsoTime;
                jObject["stale"] = staleIsoTime;

                return jObject.ToString(Formatting.None);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            return message;
            
        }

        private static double GetRandomNumber(double minimum, double maximum)
        {
            return _random.NextDouble() * (maximum - minimum) + minimum;
        }

        private static bool IsOdd(double number) => Math.Round(number, 0) % 2 == 0;
        

        /*
            ConnectionFactory factory = new ConnectionFactory();
            factory.UserName = "rapidx";
            factory.Password = "dragonfly";
            factory.HostName = "gsa.cognitics.net";
            factory.Port = 5672;
            using (var connection = factory.CreateConnection())
            using (var channel = connection.CreateModel())
            {
                channel.ExchangeDeclare("dragonfly", ExchangeType.Topic, true);

                string message = "{\"originator\":\"1152786\"," +
                                    "\"originalSender\":\"1152796\"," +
                                    "\"lat\":\"36.9254843018683\"," +
                                    "\"lon\":\"-76.02161526530303\"," +
                                    "\"entityId\":\"1152796\"," +
                                    "\"milSym\":\"SFAPMHUM------A\"," +
                                    "\"metadata\":{ \"transformTimestamp\":\"1600707048358\",\"hlaTimestamp\":\"1600707048366\"}}";

                var body = Encoding.UTF8.GetBytes(message);

                channel.BasicPublish(exchange: "dragonfly",
                                    routingKey: "dragonfly.entity_locations",
                                    basicProperties: null,
                                    body: body);
                Console.WriteLine(" [x] Sent {0}", message);
            }
         */
    }
}
