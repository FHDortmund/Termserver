using Plossum.CommandLine;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TerminologyConverter.Converter.IRMA
{
  class IrmaConverter
  {

    public IrmaConverter()
    {

    }

    public String Convert(CommandLineParser Parser)
    {
      Console.WriteLine("Converting IRMA codes...");

      String ret = "";

      try
      {
        FileInfo fileInfo = new FileInfo(Parser.ExecutablePath);
        DirectoryInfo di = new DirectoryInfo(fileInfo.DirectoryName);
        IEnumerable<FileInfo> files = di.EnumerateFiles("*.txt");

        String fileGER = "";
        String fileENG = "";

        foreach(FileInfo fi in files)
        {
          if(fi.FullName.Contains("export_ger"))
          {
            fileGER = fi.FullName;
            Console.WriteLine("Found german import File: " + fi.Name);
          }
          else if (fi.FullName.Contains("export_eng"))
          {
            fileENG = fi.FullName;
            Console.WriteLine("Found english import File: " + fi.Name);
          }
        }

        if(string.IsNullOrEmpty(fileGER) && string.IsNullOrEmpty(fileENG))
        {
          Console.WriteLine("Error while converting IRMA codes: no import file found!");
          Console.WriteLine("Either german or english file must exist containing export_eng or export_ger in filename.");

          return "";
        }

        // begin convert
        TermCSV csv = new TermCSV();
        if(string.IsNullOrEmpty(fileGER) == false)
          ParseFile(fileGER, csv, "de");
        if (string.IsNullOrEmpty(fileENG) == false)
          ParseFile(fileENG, csv, "en");

        csv.CreateFile("irma.csv");

      }
      catch(Exception ex)
      {
        ret = "Error while converting IRMA codes: " + ex.Message;
      }

      return ret;
    }

    private void ParseFile(String filename, TermCSV csv, String languageCd)
    {
      int countEntries = 0;
      int countAxis = 0;
      String[] lines = File.ReadAllLines(filename, Encoding.Default);

      int currentLevel = 0;
      TermCSVEntry currentAxis = null;
      String upperCode = "", currentCode = "";

      foreach(String line in lines)
      {
        if (string.IsNullOrEmpty(line))
          continue;

        if(line.StartsWith("**"))
        {
          String axisName = line.Replace("*","").Trim();
          currentAxis = new TermCSVEntry();
          currentAxis.Code = axisName[0].ToString().ToUpper();

          TermCSVEntry existingEntry = csv.GetEntry(currentAxis.Code);
          if (existingEntry == null)
          {
            currentAxis.Term = axisName;

            currentAxis.IsAxis = true;
            currentAxis.IsMainclass = false;
            currentAxis.IsPreferred = true;

            currentAxis.Metadata.Add("code", currentAxis.Code);

            csv.Entries.Add(currentAxis);
            countAxis++;
          }
          else
          {
            existingEntry.Translations.Add(languageCd, ExtractTerm(line));
          }
          continue;
        }

        if(currentAxis != null)
        {
          TermCSVEntry entry = new TermCSVEntry();
          String code = ExtractCode(line);
          entry.Code = currentAxis.Code + "-" + code;

          TermCSVEntry existingEntry = csv.GetEntry(entry.Code);
          if (existingEntry == null)
          {

            entry.Term = ExtractTerm(line);

            entry.IsAxis = false;
            entry.IsMainclass = false;
            entry.AssociationKind = "2";
            entry.AssociationType = "is part of";
            entry.AssociationTypeReverse = "is parent of";
            entry.IsPreferred = true;

            int level = GetLevel(line);

            if (level == 0)
            {
              entry.RelationCode = currentAxis.Code;
            }
            else
            {
              entry.RelationCode = entry.Code.Substring(0, entry.Code.Length - 1);
            }

            entry.Metadata.Add("code", code);

            csv.Entries.Add(entry);
            countEntries++;
          }
          else
          {
            existingEntry.Translations.Add(languageCd, ExtractTerm(line));
          }

          //Console.WriteLine("Found code: " + entry.Code + ", Term: " + entry.Term);

        }
        else
        {
          Console.WriteLine("Invalid file format, file must begin with axis information.");
          break;
        }
      }

      Console.WriteLine("File imported. Axis count: " + countAxis + ", Entry count: " + countEntries);

    }

    private int GetLevel(String line)
    {
      return (int)line.Count(ch => ch == '\t');
    }

    private String ExtractCode(String line)
    {
      String s = "";

      int startIndex = line.IndexOf('[');
      if (startIndex >= 0)
      {
        int endIndex = line.IndexOf(']');
        s = line.Substring(startIndex + 1, endIndex - startIndex - 1);
      }

      return s;
    }

    private String ExtractTerm(String line)
    {
      String s = "";

      int startIndex = line.IndexOf(']');
      if (startIndex >= 0)
      {
        s = line.Substring(startIndex + 1).Trim();
      }

      return s;
    }

  }
}
