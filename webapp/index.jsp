<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Script Analyser</title>

    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
     <link href="https://cdnjs.cloudflare.com/ajax/libs/startbootstrap-sb-admin-2/3.3.7+1/css/sb-admin-2.css" rel="stylesheet"> 
     <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">  

    <link href="https://fonts.googleapis.com/css?family=Lobster" rel="stylesheet">
    
</head>

<body>
	  <form method="POST" action="fileprocess" enctype="multipart/form-data">
	  <div id="wrapper">

        <!-- Navigation -->
        <nav class="navbar navbar-inverse navbar-static-top" role="navigation" style="margin-bottom: 0">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                 <a class="navbar-brand" href="home.html" style="font-family: 'Lobster', cursive; margin-left:150px;
            font-size:35px; color:#fff;"><span style="color:#2196F3;">S</span>cript 
            <span style="color:#2196F3;">A</span>nalyzer</a>

            </div>
          
        </nav>
		
		<!-- Modal -->
		  <div id="myModal" >
			<div >
			
			  <!-- Modal content-->
		
			  <div class="modal-content">
				<div class="modal-header">
				  <button type="button" class="close" data-dismiss="modal">&times;</button>
				  <h4 class="modal-title">Please Upload Your Shell Script</h4>
				</div>
				<div class="modal-body">				  
				  <input type="file" id = "xyz" name="file" required />
				</div>
				<div class="modal-footer">
				  <input type="submit" value="Analyze" />
				</div>
			  </div>
			 
			</div>
		  </div>

    </div>
  

</form>
	<script>
    var temp = document.getElementById('xyz');

    temp.onchange = function(e){ 
        var ext = this.value.match(/\.(.+)$/)[1];
        switch(ext)
        {
            case 'sh':
                break;
            default:
                alert('This file format is not supported. Please upload a shell(.sh) file');
                this.value='';
        }
    };

    </script>
	
</body>

</html>