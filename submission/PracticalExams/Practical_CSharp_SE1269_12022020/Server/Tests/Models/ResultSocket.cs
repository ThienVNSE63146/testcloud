using System;
using System.Collections.Generic;
using System.Text;

namespace Tests.Models
{
   public class ResultSocket
    {
        public Dictionary<String,String> ListQuestions { get; set; }
        public Double TotalPoint { get; set; }
        public String Time { get; set; }
        public int Result { get; set; }

    }
}
