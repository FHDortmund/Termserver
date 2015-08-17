using FileHelpers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter.Converter.ATC
{
  [DelimitedRecord(";")]

  public class ATC_Entry
  {
    public String ATC_CODE;

    [FieldQuoted('"', QuoteMode.OptionalForBoth)]
    public String BEDEUTUNG;

    [FieldQuoted('"', QuoteMode.OptionalForBoth)]
    public String DDD_INFO;


    public String temp1;
    public String temp2;
    public String temp3;
    //public String temp4;
    //public String temp5;

  }
}
