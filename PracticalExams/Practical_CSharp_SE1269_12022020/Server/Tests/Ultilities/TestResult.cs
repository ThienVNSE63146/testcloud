using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using Tests.Models;

namespace Tests.Ultilities
{
    public class TestResult
    {
        public static ResultSocket resultSocket { get; set; }
        public static void WriteResult(string path,string result)
        {
            if(File.Exists(path))
            {
                using(StreamWriter sw = File.AppendText(path))
                {
                    sw.WriteLine(result);
                }
            }else
            {
               using (StreamWriter sw = File.CreateText(path))
                {
                    sw.WriteLine(result);
                }
            }
        }
        public static Dictionary<String, String> splitQuestions(String questionPointStr)
        {
            Dictionary<String, String> storeQuestion = new Dictionary<string, string>();
            List<String> splited = questionPointStr.Split("-").ToList();
            foreach (var item in splited)
            {
                String[] itemSplited = item.Split(":");
                storeQuestion.Add(itemSplited[0], itemSplited[1]);
            }
            return storeQuestion;
        }

        public static void connectToServer(int port,String data)
        {
            IPAddress[] ipAddress = Dns.GetHostAddresses(getLocalIPAddress());
            IPEndPoint ipEnd = new IPEndPoint(ipAddress[0], port);
            Socket clientSock = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.IP);
            clientSock.Connect(ipEnd);
            byte[] dataByte = Encoding.ASCII.GetBytes(data);
            byte[] clientData = new byte[4 + dataByte.Length];
            byte[] dataLen = BitConverter.GetBytes(dataByte.Length);
            dataLen.CopyTo(clientData, 0);
            dataByte.CopyTo(clientData, 4);
            clientSock.Send(clientData);
        }
        //some methods => coding convension.
         public static string getLocalIPAddress()
         {
             var host = Dns.GetHostEntry(Dns.GetHostName());
             foreach (var ip in host.AddressList)
             {
                 if (ip.AddressFamily == AddressFamily.InterNetwork)
                 {
                     return ip.ToString();
                 }
             }
             throw new Exception("No network adapters with an IPv4 address in the system!");
         }
    }
}
