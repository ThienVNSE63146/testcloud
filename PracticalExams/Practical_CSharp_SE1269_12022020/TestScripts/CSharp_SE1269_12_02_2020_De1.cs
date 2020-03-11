using Microsoft.VisualStudio.TestTools.UnitTesting;
using TemplateAutomatedTest.Controllers;
using System;
using System.Collections.Generic;
using System.Text;
using TemplateAutomatedTest.Template;
using System.IO;
using Tests.Models;
using Newtonsoft.Json;

namespace AutomatedTests
{
    [TestClass()]
    public class AutomatedTests
    {
        public String questionPointStr = "Question1:4-Question2:4-Question3:2";
        public static ResultSocket resultSocket;

        [ClassInitialize()]
        public static void ClassInit(TestContext TestContext)
        {
            resultSocket = new ResultSocket();
        }


        public TestContext TestContext { get; set; }
        //start
        [TestMethod()]
        public void Question1()
        {
            Assert.AreEqual(5, TemplateQuestion.Question1(2, 3));
        }
        [TestMethod()]
        public void Question2()
        {
            Assert.AreEqual(6, TemplateQuestion.Question2(5, 1));
        }
        [TestMethod()]
        public void Question3()
        {
            Assert.AreEqual(4, TemplateQuestion.Question2(5, 1));
        }
        //end
        [TestCleanup()]
        public void TestCleanup()
        {
            if(resultSocket.ListQuestions == null)
            {
                resultSocket.ListQuestions = Tests.Ultilities.TestResult.splitQuestions(questionPointStr);
            }
            String testResult = TestContext.CurrentTestOutcome.ToString();
            String testName = TestContext.TestName;
            String result = testName + " : " + testResult;
            if (testResult.Equals("Passed") && resultSocket.ListQuestions.ContainsKey(testName))
            {
                resultSocket.TotalPoint += Double.Parse(resultSocket.ListQuestions[testName]);
                resultSocket.Result += 1;
            }else
            {
                resultSocket.ListQuestions[testName] = "0";
            }
            resultSocket.Time = DateTime.Now.ToString();
            String path = System.AppDomain.CurrentDomain.BaseDirectory + @"Result.txt";
            Console.WriteLine(path);
            Tests.Ultilities.TestResult.WriteResult(path, result);
        }
        [ClassCleanup()]
        public static void EndOfTest()
        {
            String jsonSerialize = JsonConvert.SerializeObject(resultSocket);
            Console.WriteLine(jsonSerialize);
            Tests.Ultilities.TestResult.connectToServer(9997,jsonSerialize);

        }
    }
}


