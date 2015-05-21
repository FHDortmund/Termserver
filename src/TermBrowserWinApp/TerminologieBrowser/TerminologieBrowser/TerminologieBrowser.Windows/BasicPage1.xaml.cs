using TerminologieBrowser.Common;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// Die Elementvorlage "Standardseite" ist unter http://go.microsoft.com/fwlink/?LinkId=234237 dokumentiert.

namespace TerminologieBrowser
{
  /// <summary>
  /// Eine Standardseite mit Eigenschaften, die die meisten Anwendungen aufweisen.
  /// </summary>
  public sealed partial class BasicPage1 : Page
  {

    private NavigationHelper navigationHelper;
    private ObservableDictionary defaultViewModel = new ObservableDictionary();

    /// <summary>
    /// Dies kann in ein stark typisiertes Anzeigemodell geändert werden.
    /// </summary>
    public ObservableDictionary DefaultViewModel
    {
      get { return this.defaultViewModel; }
    }

    /// <summary>
    /// NavigationHelper wird auf jeder Seite zur Unterstützung bei der Navigation verwendet und 
    /// Verwaltung der Prozesslebensdauer
    /// </summary>
    public NavigationHelper NavigationHelper
    {
      get { return this.navigationHelper; }
    }


    public BasicPage1()
    {
      this.InitializeComponent();
      this.navigationHelper = new NavigationHelper(this);
      this.navigationHelper.LoadState += navigationHelper_LoadState;
      this.navigationHelper.SaveState += navigationHelper_SaveState;
    }

    /// <summary>
    /// Füllt die Seite mit Inhalt auf, der bei der Navigation übergeben wird. Gespeicherte Zustände werden ebenfalls
    /// bereitgestellt, wenn eine Seite aus einer vorherigen Sitzung neu erstellt wird.
    /// </summary>
    /// <param name="sender">
    /// Die Quelle des Ereignisses, normalerweise <see cref="NavigationHelper"/>
    /// </param>
    /// <param name="e">Ereignisdaten, die die Navigationsparameter bereitstellen, die an
    /// <see cref="Frame.Navigate(Type, Object)"/> als diese Seite ursprünglich angefordert wurde und
    /// ein Wörterbuch des Zustands, der von dieser Seite während einer früheren
    /// beibehalten wurde. Der Zustand ist beim ersten Aufrufen einer Seite NULL.</param>
    private void navigationHelper_LoadState(object sender, LoadStateEventArgs e)
    {
    }

    /// <summary>
    /// Behält den dieser Seite zugeordneten Zustand bei, wenn die Anwendung angehalten oder
    /// die Seite im Navigationscache verworfen wird.  Die Werte müssen den Serialisierungsanforderungen
    /// von <see cref="SuspensionManager.SessionState"/> entsprechen.
    /// </summary>
    /// <param name="sender">Die Quelle des Ereignisses, normalerweise <see cref="NavigationHelper"/></param>
    /// <param name="e">Ereignisdaten, die ein leeres Wörterbuch zum Auffüllen bereitstellen
    /// serialisierbarer Zustand.</param>
    private void navigationHelper_SaveState(object sender, SaveStateEventArgs e)
    {
    }

    #region NavigationHelper-Registrierung

    /// Die in diesem Abschnitt bereitgestellten Methoden werden einfach verwendet, um
    /// damit NavigationHelper auf die Navigationsmethoden der Seite reagieren kann.
    /// 
    /// Platzieren Sie seitenspezifische Logik in Ereignishandlern für  
    /// <see cref="GridCS.Common.NavigationHelper.LoadState"/>
    /// und <see cref="GridCS.Common.NavigationHelper.SaveState"/>.
    /// Der Navigationsparameter ist in der LoadState-Methode verfügbar 
    /// zusätzlich zum Seitenzustand, der während einer früheren Sitzung beibehalten wurde.

    protected override void OnNavigatedTo(NavigationEventArgs e)
    {
      navigationHelper.OnNavigatedTo(e);
    }

    protected override void OnNavigatedFrom(NavigationEventArgs e)
    {
      navigationHelper.OnNavigatedFrom(e);
    }

    #endregion
  }
}
