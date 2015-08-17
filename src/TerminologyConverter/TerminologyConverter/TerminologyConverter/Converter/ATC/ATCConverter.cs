using Plossum.CommandLine;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter.Converter.ATC
{
  class ATCConverter
  {
    Options options;

    public ATCConverter(Options Options)
    {
      options = Options;
    }

    public String Convert(CommandLineParser Parser)
    {
      Console.WriteLine("Converting ATC codes...");

      String ret = "";
      
      try
      {
        FileInfo fileInfo = new FileInfo(Parser.ExecutablePath);
        DirectoryInfo di = new DirectoryInfo(fileInfo.DirectoryName);
        IEnumerable<FileInfo> files = di.EnumerateFiles("*.csv");

        String filename = options.Filename;

        if (string.IsNullOrEmpty(options.Filename))
        {
          foreach (FileInfo fi in files)
          {
            if (fi.FullName.Contains("atc"))
            {
              filename = fi.FullName;
              Console.WriteLine("Found import File: " + fi.Name);
              break;
            }
          }
        }

        if (string.IsNullOrEmpty(filename))
        {
          Console.WriteLine("Error while converting ATC codes: no import file found! Please specify with Filename.");
          return "";
        }

        // begin convert
        TermCSV csv = new TermCSV();
        if (string.IsNullOrEmpty(filename) == false)
          ParseFile(filename, csv, "de");

        csv.CreateFile("atc_termserver.csv");
      }
      catch (Exception ex)
      {
        ret = "Error while converting ATC codes: " + ex.Message;
      }

      return ret;
    }


    private void ParseFile(String filename, TermCSV csv, String languageCd)
    {
      int countEntries = 0;
      int countAxis = 0;

      var engine = new FileHelpers.FileHelperEngine<ATC_Entry>();

      List<ATC_Entry> entries = engine.ReadFileAsList(filename);
      List<String> codeList = new List<string>();
      
      TermCSVEntry currentAxis = null;

      foreach (ATC_Entry atc_entry in entries)
      {
        if (atc_entry == null || string.IsNullOrEmpty(atc_entry.ATC_CODE))
          continue;

        if (atc_entry.ATC_CODE.Length == 1)
        {
          currentAxis = new TermCSVEntry();
          currentAxis.Code = atc_entry.ATC_CODE;

          currentAxis.Term = atc_entry.BEDEUTUNG;

          currentAxis.IsAxis = true;
          currentAxis.IsMainclass = false;
          currentAxis.IsPreferred = true;

          currentAxis.Metadata.Add("DDD-INFO", atc_entry.DDD_INFO);

          csv.Entries.Add(currentAxis);
          countAxis++;
          codeList.Add(atc_entry.ATC_CODE);

          continue;
        }


        if (currentAxis != null)
        {
          TermCSVEntry entry = new TermCSVEntry();

          entry.Code = atc_entry.ATC_CODE;
          entry.Term = atc_entry.BEDEUTUNG;

          entry.IsAxis = false;
          entry.IsMainclass = false;
          entry.AssociationKind = "2";
          entry.AssociationType = "is part of";
          entry.AssociationTypeReverse = "is parent of";
          entry.IsPreferred = true;


          String code = atc_entry.ATC_CODE;
          while(code.Length > 0)
          {
            code = code.Substring(0, code.Length - 1);

            if(codeList.Contains(code))
            {
              entry.RelationCode = code;
              break;
            }
          }

          entry.Metadata.Add("DDD-INFO", atc_entry.DDD_INFO);

          csv.Entries.Add(entry);
          countEntries++;

          if (codeList.Contains(atc_entry.ATC_CODE))
          {
            // WARNING
            codeList.Add(atc_entry.ATC_CODE);
          }
          else
            codeList.Add(atc_entry.ATC_CODE);


          //Console.WriteLine("Found code: " + entry.Code + ", Term: " + entry.Term);

        }
        
      }

      Console.WriteLine("File imported. Axis count: " + countAxis + ", Entry count: " + countEntries);

    }
  }
}
