$(document).ready(function(){

	$("#getfiles").click(function(){
		$.ajax({
			url: '/listfiles',
		}).done(function(data){
			var fileHTML = "";
			for(file of data) {
				fileHTML += '<li class="list-group-item"><img src="' + file.thumbnailLink + '">'
					+ file.name + ' (FileID : ' + file.id + ')'
					+ '<button onclick="downloadfile(\'' + file.id + '\')">Download</button>';
			}
			$("#fileListContainer").html(fileHTML);
		});
	});
});

function downloadfile(id) {
	$.ajax({
		url: '/downloadFile/' + id,
		method: 'GET'
	}).done(function(){
		alert('File download complete.');
	});
}
