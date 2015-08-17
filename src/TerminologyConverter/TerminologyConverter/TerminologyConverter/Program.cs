using Plossum.CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter
{
  class Program
  {
    static int consoleWidth = 78;

    static int Main(string[] args)
    {
      //Console.WriteLine("Anzahl: " + args.Count());

      Options options = new Options();
      CommandLineParser parser = new CommandLineParser(options);
      parser.Parse();
      Console.WriteLine(parser.UsageInfo.GetHeaderAsString(consoleWidth));

      if (options.Help)
      {
        Console.WriteLine(parser.UsageInfo.GetOptionsAsString(consoleWidth));
        StopApp();
        return 0;
      }
      else if (parser.HasErrors)
      {
        Console.WriteLine(parser.UsageInfo.GetErrorsAsString(consoleWidth));
        StopApp();
        return -1;
      }
      //Console.WriteLine("Hello {0}!", options.Name);

      Converter.Converter conv = new Converter.Converter(parser, options);
      String ret = conv.StartConverting();

      Console.WriteLine(ret);

      


      StopApp();
      return 0;
    }

    static void StopApp()
    {
      if (System.Diagnostics.Debugger.IsAttached)
      {
        Console.WriteLine("Press enter to close...");
        Console.ReadLine();
      }
    }

  }
}
