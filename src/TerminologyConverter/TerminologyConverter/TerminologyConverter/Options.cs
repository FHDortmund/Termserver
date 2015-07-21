using Plossum.CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter
{
  [CommandLineManager(ApplicationName = "Terminology Converter",
    Copyright = "Copyright (c) Robert Mützner")]
  class Options
  {
    [CommandLineOption(Description = "Displays this help text")]
    public bool Help = false;

    [CommandLineOption(Description = "Specifies the terminology format", MinOccurs = 1)]
    public string Format
    {
      get { return format; }
      set
      {
        if (String.IsNullOrEmpty(value))
          throw new InvalidOptionValueException(
              "The format must not be empty", false);
        format = value;
      }
    }

    private string format;
  }
}
