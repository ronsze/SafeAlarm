const http = require('http');
const https = require('https');
const bodyParser = require('body-parser');
const dbsecret = require('./mydb.json')
const mysql = require('mysql2');
const app = require('express')();
const { PythonShell } = require('python-shell');
const nodeStatic = require('node-static')
const fs = require('fs')
const multer = require('multer')
const upload = multer({dest: 'uploads/'})
const crypto = require('crypto')
const openssl = require('openssl-nodejs');
const password = 'github';
const urlencode = require('urlencode')
const db = mysql.createConnection(
  dbsecret
);

const options_SSL = {
  key: fs.readFileSync('./openssl/rootca.key'),
  cert: fs.readFileSync('./openssl/rootca.crt'),
  requestCert: false,
  rejectUnauthorized: false
};

const httpsServer = https.createServer(options_SSL, app);
var io = require('socket.io')(httpsServer, {secure:true});

var fileServer = new(nodeStatic.Server)();
app.use(bodyParser.json({limit: '50mb'}))
app.use(bodyParser.urlencoded({limit: '50mb', extended: true}))


app.get('/ex', (req, res) => {
  res.send("히히")
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.post(`/db/create`, (req, res) => {    //서버에 id 최초생성
  console.log("id생성")
  id = req.body.id
  db.query(`SELECT EXISTS (SELECT * FROM user where id = ${id}) as success;`, function(err, result){
    if(result[0].success == 1){
      console.log("id생성 실패")
      res.send("실패~")
    }
    else {
      console.log(`id생성 성공, ${id}`)
      db.query(`INSERT INTO user(id) VALUES(${id});`)
      res.send({"result":"성공"});
    }
  })
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.post('/regist/guard', (req, res) => {
  console.log('보호자 등록')
  id = req.body.id
  db.query(`SELECT destination FROM user WHERE id = ${id}`, function(err, result){
    if(result[0].destination != null){
      console.log("보호자 등록 성공")
      res.send({"result":"성공"})
    }else{
      console.log("보호자 등록 실패")
      res.send("실패")
    }
  })
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.post('/regist/ward', (req, res) => {
  console.log('피보호자 등록')
  id = req.body.id
  code = req.body.code
  db.query(`SELECT id, destination FROM user WHERE id = ${code}`, function(err, result){
    console.log(result[0].destination);
    if(result[0].destination == null){
      db.query(`UPDATE user SET destination = ${code} where id = ${id};`);
      db.query(`UPDATE user SET destination = ${id} where id = ${code};`);
      console.log("피보호자 등록 성공")
      res.send({"result":"성공"});
    }else{
      console.log("피보호자 등록 실패 이미 등록됨")
      res.send("등록 실패")
    }
  })
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.post("/db/missingInfo", (req, res) => {
  var id = req.body.id
  var info = JSON.parse(req.body.info)
  var name = '"' + info.name + '"'
  var age = '"' + info.age + '"'
  var sex = '"' + info.sex + '"'
  var height = '"' + info.height + '"'
  var number= '"' + info.number + '"'
  var extra = '"' + info.extra + '"'
  extra = extra.replace("\n", ' ')
  var looks = '"' + info.looks + '"'
  looks = looks.replace("\n", ' ')
  var extra2 = '"' + info.extra2 + '"'
  extra2 = extra2.replace("\n", ' ')
  //var loc = '"' + info.loc + '"'
  var loc = '"' + info.loc + '"'
  db.query(`SELECT EXISTS (SELECT * FROM user where id = ${id}) as success`, function(err, result){
    if(result[0].success == 0){
      res.send("실패")
    }else{
      db.query(`SELECT EXISTS (SELECT * FROM msinfo where id = ${id}) as success`, function(err, result){
        if(result[0].success == 0){
          db.query(`INSERT INTO msinfo(id, name, age, sex, height, number, extra, looks, extra2, loc, time)
          VALUES(${id}, ${name}, ${age}, ${sex}, ${height}, ${number}, ${extra}, ${looks}, ${extra2}, ${loc}, (SELECT NOW()));`);
        }else{
          db.query(`UPDATE msinfo SET name = ${name}, age = ${age}, sex = ${sex}, height = ${height}, number = ${number},
                    extra = ${extra}, looks = ${looks}, extra2 = ${extra2}, loc = ${loc}, time = (SELECT NOW() WHERE id = ${id});`)
        }
      });
      res.send({"result":"성공"})
    }
  });
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
var _storage = multer.diskStorage({
	destination: 'uploads/',
	filename: function(req, file, cb) {
		return crypto.pseudoRandomBytes(16, function(err, raw) {
			if(err) {
				return cb(err);
			}
			return cb(null, file.originalname);
		});
	}
});
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.post("/db/missingPhoto", multer({storage: _storage}).single('file'), (req, res) => {
  try {
		let file = req.file;
		let originalName = '';
		let fileName = '';
		let mimeType = '';
		let size = 0;

		if(file) {
			originalName = file.originalname;
			filename = file.fileName
			mimeType = file.mimetype;
			size = file.size;
		} else{
		}
	} catch (err) {
		console.dir(err.stack);
	}


  var id = req.file.originalname.substr(0, req.file.originalname.length-4)

  let imgData = readImageFile(`./uploads/${id}.png`)

  let sql = `UPDATE msinfo SET photo = BINARY(?) WHERE id = ?`

  db.query(sql, [imgData, id], (err, rows, fields) => {
    if(err === null){
      console.log("성공")
      res.redirect("/uploads/" + req.file.originalname);
    }else{
      console.log(String(err))
      res.send("실패")
    }
  })
})

function readImageFile(file){
  const bitmap = fs.readFileSync(file);
  const buf = new Buffer.from(bitmap)
  return buf
}
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.get("/db/getMissingInfo", (req, res) => {
  db.query(`SELECT * FROM msinfo`, (err, rows, fields) => {
    if(err === null){
      res.send({"result":rows})
    }else{
      console.log(String(err))
    }
  })
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.post("/ca/createCert", (req, res) => {
  var id = req.body.id
  var csr = req.body.CSR
  fs.exists(`./openssl/user/${id}.crt`, function(exists){
    if(exists){
      res.send("실패")
    }else{
      fs.writeFile(`./openssl/user/${id}.csr`, csr, function(err, result){
        if(err === null){
          createCert(id)
          res.send({"result":"성공"})
        }else{
          console.log(err)
          res.send("실패")
        }
      })
    }
  })
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.get("/ca/getCert", (req, res) => {
  fs.exists(`./openssl/user/${req.query.id}.crt`, function(exists){
    if(!exists){
      console.log("파일없음")
      res.send("실패")
    }else{
      fs.readFile(`./openssl/user/${req.query.id}.crt`, function(err, result){
        if(err === null){
          res.send({"result":`${result}`})
        }else{
          console.log("err : " + String(err))
          res.send("실패")
        }
      })
    }
  });
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
function createCert(id){
  openssl(`openssl x509 -req -days 365 -extensions v3_user -extfile host_openssl.conf -CA rootca.crt -CAcreateserial -CAkey rootca.key -in ./user/${id}.csr -out ./user/${id}.crt`, function(err, buffer){
    if(err === null){

    }else{
      console.log(String(err))
    }
  })
}
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
app.get("ca/getCaCert", (req, res) => {
})

function getPrimeNumber(){
  let options = {
    scriptPath: 'C:/Users/ljg58/AppData/Local/Programs/Python/Python39',
    args: []
  }
  return new Promise(resolve =>{
    PythonShell.run('getPrime.py', options, function(err, data){
      if(err) throw err;
      resolve(data)
    });
  });
}

app.post("/db/deleteInfo", (req, res) => {
  var id = req.body.id
  console.log(id)

  let sql = `DELETE FROM msinfo WHERE id = ?`
  db.query(sql, [id], (err, rows, fields) => {
    if(err === null){
      res.send({"result":"성공"})
    }else{
      console.log(String(err))
      res.send("실패")
    }
  })
})
//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
io.on('connection', function(socket) {
  var roomID = ""

  socket.on('enterRoom', (room) =>{
    roomID = room
    socket.join(roomID)
    console.log(socket.id)
    //io.of('/').in(roomID).clients(function(error,clients){
    //  console.log(clients.len gth);
    //  console.log(socket.id)
    //});
  });

  socket.on('enter', () =>{
    socket.join('123')
    console.log("enter")
    io.to('123').emit('Message')
  })

  socket.on('requestLoc', (str) => {
    console.log("reuqestLoc : "+str)
    io.to(roomID).emit('requestLoc', str)
  });

  socket.on('getPrime', (id) =>{
    console.log("getPrime" + id)
    getPrimeNumber().then(function(resolvedData){
      io.to(roomID).emit('callbackPrime', resolvedData)
      console.log(resolvedData)
    })
  })

  socket.on("callbackLoc", (location) =>{
    console.log(location)
    io.to(roomID).emit('callbackLoc', location)
  });

  socket.on('disconnect', function() {
    console.log('user disconnected: ' + socket.id);
    io.to(roomID).emit('destDisconnect')
    io.of('/').in(roomID).clients(function(error,clients){
      console.log(clients.length);
    });
  });

  socket.on("sendPrime", (primeStr) =>{
    console.log("sendPrime : " + primeStr)
    io.to(roomID).emit("receivePrime", primeStr)
  });

  socket.on("sendR1", (r1) =>{
    console.log("sendR1 : " + r1)
    io.to(roomID).emit("receiveR1", r1)
  });

  socket.on("sendR2", (r2) =>{
    console.log("sendR2 : " + r2)
    io.to(roomID).emit("receiveR2", r2)
  });

  socket.on("sendCert", () =>{
    io.to(roomID).emit("sendCert")
  })

  socket.on("HelpCall_W", () =>{
    io.to(roomID).emit("HelpCall_W");
  });

  socket.on("sendCerti", () =>{
    io.to(roomID).emit("sendCerti")
  })

  socket.on("callbackCert", () =>{
    console.log("callbackCert")
    io.to(roomID).emit("callbackCert")
  })

  socket.on("postMissing", () =>{
    console.log("newMissing")
    io.emit("newMissing")
  })
});

httpsServer.listen(443, function () {
    console.log("HTTPS server listening on port " + 443);
});
