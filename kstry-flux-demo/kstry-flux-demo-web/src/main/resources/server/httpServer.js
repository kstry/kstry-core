var
	http = require('http'),
	fs = require('fs')

var server = http.createServer(function(request, response) {
	var url = request.url.substr(1);
	setTimeout(() => {
		fs.readFile('./data.json', function(err, data) {
			if (!err) {
				response.writeHead(200, {
					"Content-Type": "application/json; charset=utf-8"
				});
				var reqData = '';
				request.on('data', function (raw) {
        		    reqData += raw;
        		});
        		request.on('end', function () {
        		    var d = JSON.parse(data);
					console.log(new Date().toLocaleString() + " - request data: " + decodeURI(reqData) + ". response data: " + JSON.stringify(d[url]));
					response.end(JSON.stringify(d[url]));
        		});
			} else {
				throw err;
			}
		})
	}, 0)
});
server.listen(8787);
console.log("server is running at http://127.0.0.1:8787");