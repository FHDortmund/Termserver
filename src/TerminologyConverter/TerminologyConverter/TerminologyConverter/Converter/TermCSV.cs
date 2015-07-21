using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter.Converter
{
  class TermCSV
  {
    public TermCSV()
    {
      Entries = new List<TermCSVEntry>();
    }
    public String CodesystemName { get; set; }
    public String CodesystemVersionName { get; set; }

    public List<TermCSVEntry> Entries { get; set; } 


    public TermCSVEntry GetEntry(String code)
    {
      foreach(TermCSVEntry entry in Entries)
      {
        if (entry.Code == code)
          return entry;
      }
      return null;
    }


    public void CreateFile()
    {
      List<String> fileContent = new List<string>();

      List<String> languageList = new List<string>();
      List<String> metadataList = new List<string>();


      foreach (TermCSVEntry entry in Entries)
      {
        fileContent.Add(entry.ToString());

        foreach (String key in entry.Translations.Keys)
        {
          if (languageList.Contains(key) == false)
            languageList.Add(key);
        }

        foreach (String key in entry.Metadata.Keys)
        {
          if (metadataList.Contains(key) == false)
            metadataList.Add(key);
        }

      }


      fileContent.Insert(0, CreateHeader(languageList, metadataList));

      File.WriteAllLines("irma.csv", fileContent.ToArray(), Encoding.Default);
    }

    private String CreateHeader(List<String> languageList, List<String> metadataList)
    {
      String s = "code;term;term_abbrevation;description;is_preferred;is_axis;is_mainclass;relation;association_kind;association_type;association_type_reverse;crossmapping_csv_id";

      foreach(String lang in languageList)
      {
        s += ";translation_" + lang;
      }

      foreach (String md in metadataList)
      {
        s += ";metadata_" + md;
      }

      return s;
    }
  }
}
