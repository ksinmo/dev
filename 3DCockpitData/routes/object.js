var express = require('express');
var router = express.Router();
var pgp = require('pg-promise')(/*options*/)
var db = pgp('postgres://cym:cym@mdmcloud.tobeway.com:5432/postgres')

/* GET users listing. */
router.get('/', function(req, res, next) {
    var query = 'SELECT * FROM cym."Object"';
    db.any(query)
    .then(function (data) {
        res.send(data);
    })
    .catch(function (error) {
        next(error)
    })
});
router.get('/:objID', function(req, res, next) {
    var query = 'SELECT * FROM cym."Object" WHERE "ObjID" = $1';
    db.one(query, req.params.objID)
    .then(function (data) {
        res.send(data);
    })
    .catch(function (error) {
        next(error)
    })
});

router.post('/', function(req, res, next) {
    res.send('here is class post');
});
router.put('/', function(req, res, next) {
    res.send('here is class put');
});
router.delete('/', function(req, res, next) {
    res.send('here is class delete');
});
      
module.exports = router;
