
CKEDITOR.editorConfig = function(config) {

  config.extraPlugins = 'uicolor';
  config.resize_enabled = false;
  config.scayt_autoStartup = true;
  config.toolbar_emptyToolbar =
         [
           {name: 'empty', items: []}
         ];
};

/*CKEDITOR.editorConfig = function(config) {
 config.resize_enabled = false;
 config.toolbar = 'MyToolbar';
 config.toolbar_MyToolbar = [
 ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript',
 'Superscript', 'TextColor', 'BGColor', '-', 'Cut', 'Copy',
 'Paste', 'Link', 'Unlink', 'Image'],
 ['Undo', 'Redo', '-', 'JustifyLeft', 'JustifyCenter',
 'JustifyRight', 'JustifyBlock'],
 ['Table', 'Smiley', 'SpecialChar', 'PageBreak',
 'Styles', 'Format', 'Font', 'FontSize', 'Maximize',
 'UIColor']];
 };*/
