// Delay loading any function until the html dom has loaded. All functions are
// defined in this top level function to ensure private scope.
jQuery(document).ready(function () {

  // Installs error handling.
  jQuery.ajaxSetup({
  error: function(resp, e) {
    if (resp.status == 0){
      alert('You are offline!!\n Please Check Your Network.');
      } else if (resp.status == 404){
        alert('Requested URL not found.');
      } else if (resp.status == 500){
        alert('Internel Server Error:\n\t' + resp.responseText);
      } else if (e == 'parsererror') {
        alert('Error.\nParsing JSON Request failed.');
      } else if (e == 'timeout') {
        alert('Request timeout.');
      } else {
        alert('Unknown Error.\n' + resp.responseText);
      }
    }
  });  // error:function()


  var generate_btn = jQuery('#generate_btn');
  var generate_btn1 = jQuery('#generate_btn1');

  var svg_div = jQuery('#graphviz_svg_div');
  var graphviz_data_textarea = jQuery('#graphviz_data');
    
  var svg_div1 = jQuery('#graphviz_svg_div1');
  var graphviz_data_textarea1 = jQuery('#graphviz_data1');

  function InsertGraphvizText(text) {
    graphviz_data_textarea.val(text);
  }

  function InsertGraphvizText1(text) {
    graphviz_data_textarea1.val(text);
  }

  function UpdateGraphviz() {
	svg_div.html("");
    var data = graphviz_data_textarea.val();
    // Generate the Visualization of the Graph into "svg".
    var svg = Viz(data, "svg");
    svg_div.html("<hr>Code Block Diagram</hr><hr>"+svg);
  }

function UpdateGraphviz1() {
	svg_div1.html("");
    var data1 = graphviz_data_textarea1.val();
    // Generate the Visualization of the Graph into "svg".
    var svg = Viz(data1, "svg");
    svg_div1.html("<hr>Complete Diagram</hr><hr>"+svg);
  }
    
  // Startup function: call UpdateGraphviz
  jQuery(function() {
	// The buttons are disabled, enable them now that this script
	// has loaded.
    generate_btn.removeAttr("disabled")
                .text("Show Diagram(Code Block)");
    generate_btn1.removeAttr("disabled")
                .text("Show Diagram(Complete)");
  });

  // Bind actions to form buttons.
  generate_btn.click(UpdateGraphviz);
generate_btn1.click(UpdateGraphviz1);    


});
