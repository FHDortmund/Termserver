using Plossum.CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TerminologyConverter.Converter.ATC;
using TerminologyConverter.Converter.IRMA;

namespace TerminologyConverter.Converter
{
  public class Converter
  {
    String format;
    CommandLineParser parser;
    Options options;

    public Converter(CommandLineParser Parser, Options _options)
    {
      
      parser = Parser;
      options = _options;
      format = options.Format;
    }


    public String StartConverting()
    {
      String s_format = format.ToLower();

      String ret = "";

      if(s_format == "irma")
      {
        IrmaConverter converter = new IrmaConverter();
        return converter.Convert(parser);
      }
      else if (s_format == "atc")
      {
        ATCConverter converter = new ATCConverter(options);
        return converter.Convert(parser);
      }
      else
      {
        return "Format '" + format + "' not supported. Please use one of the following formats:\n"
          + "'irma'";
      }
      

      return ret;
    }

  }
}
