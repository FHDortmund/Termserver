using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Popups;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// Die Elementvorlage "Leere Seite" ist unter http://go.microsoft.com/fwlink/?LinkId=234238 dokumentiert.

namespace TerminologieBrowser
{
  /// <summary>
  /// Eine leere Seite, die eigenständig verwendet werden kann oder auf die innerhalb eines Frames navigiert werden kann.
  /// </summary>
  public sealed partial class MainPage : Page
  {
    public MainPage()
    {
      this.InitializeComponent();

      InitData();
    }

    private async void InitData()
    {
      // get TS version
      SearchClient.SearchClient search = new SearchClient.SearchClient();
      SearchClient.GetTermserverVersionResponse response = await search.GetTermserverVersionAsync();
      tbVersion.Text = response.@return.version;

      // get codesystem list
      SearchClient.listCodeSystemsRequestType lcsRequest = new SearchClient.listCodeSystemsRequestType();
      SearchClient.ListCodeSystemsResponse lcsResponse = await search.ListCodeSystemsAsync(lcsRequest);

      if(lcsResponse.@return.returnInfos.status == SearchClient.status.OK)
      {
        itemListView.ItemsSource = lcsResponse.@return.codeSystem;
      }
      else
      {
        MessageDialog dlg = new MessageDialog(lcsResponse.@return.returnInfos.message); 
        await dlg.ShowAsync();
      }

    }
  }
}
