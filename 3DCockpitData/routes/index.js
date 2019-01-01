var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  //res.render('index', { title: 'Express' });
  const { Client } = require('pg')
  const client = new Client({
    user: 'cym',
    host: 'mdmcloud.tobeway.com',
    database: 'postgres',
    password: 'cym',
    port: 5432,
  });
  client.connect();
  client.query('SELECT * FROM cym."Class"', (err, ressql) => {
    res.send(ressql.rows)
    client.end()
  })

});

module.exports = router;
