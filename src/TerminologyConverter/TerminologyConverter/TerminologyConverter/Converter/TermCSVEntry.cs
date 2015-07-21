using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter.Converter
{
  class TermCSVEntry
  {
    private static String DELIMITER = ";";

    public TermCSVEntry()
    {
      Translations = new Dictionary<string, string>();
      Metadata = new Dictionary<string, string>();
    }

    public String Code { get; set; }
    public String Term { get; set; }
    public String TermAbbrevation { get; set; }
    public String Description { get; set; }
    public Boolean IsPreferred { get; set; }
    public Boolean IsAxis { get; set; }
    public Boolean IsMainclass { get; set; }
    
    public Dictionary<String, String> Translations { get; set; }

    public Dictionary<String, String> Metadata { get; set; }

    public String RelationCode { get; set; }
    public String AssociationKind { get; set; }
    public String AssociationType { get; set; }
    public String AssociationTypeReverse { get; set; }
    public long CrossmappingCsvId { get; set; }

    
    public  String ToString()
    {
      String s = Code;

      s += DELIMITER + Term;

      s += DELIMITER + PrepareStr(TermAbbrevation);
      s += DELIMITER + PrepareStr(Description);
      s += DELIMITER + BooleanValue(IsPreferred);
      s += DELIMITER + BooleanValue(IsAxis);
      s += DELIMITER + BooleanValue(IsMainclass);

      s += DELIMITER + PrepareStr(RelationCode);
      s += DELIMITER + PrepareStr(AssociationKind);
      s += DELIMITER + PrepareStr(AssociationType);
      s += DELIMITER + PrepareStr(AssociationTypeReverse);
      if(CrossmappingCsvId > 0)
        s += DELIMITER + CrossmappingCsvId;
      else s += DELIMITER;

      foreach(String key in Translations.Keys)
      {
        //s += DELIMITER + "translation_" + key.ToLower() + 
        s += DELIMITER + Translations[key];
      }

      foreach (String key in Metadata.Keys)
      {
        //s += DELIMITER + "translation_" + key.ToLower() + 
        s += DELIMITER + Metadata[key];
      }

      return s;
    }

    public String PrepareStr(String s)
    {
      if (s == null)
        return "";

      return s;
    }

    public String BooleanValue(Boolean value)
    {
      if (value == null)
        return "";
      if (value == true)
        return "1";
      else if (value == false)
        return "0";

      return "";
    }

  }
}
