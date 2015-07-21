using Plossum.CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TerminologyConverter.Converter.IRMA;

namespace TerminologyConverter.Converter
{
  public class Converter
  {
    String format;
    CommandLineParser parser;

    public Converter(CommandLineParser Parser, String Format)
    {
      format = Format;
      parser = Parser;

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
      else
      {
        return "Format '" + format + "' not supported. Please use one of the following formats:\n"
          + "'irma'";
      }

      return ret;
    }

  }
}
