using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using TemplateAutomatedTest.Student;

namespace TemplateAutomatedTest.Template
{
    public class TemplateQuestion
    {
        public static int Question1(int number1,int number2)
        {
            return StudentWork.AddTwoNumber(number1, number2);
        }
        public static int Question2(int number1, int number2)
        {
            return StudentWork.MinusTwoNumber(number1, number2);
        }
    }
}
