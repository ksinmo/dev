var express = require('express');
var router = express.Router();
var pgp = require('pg-promise')(/*options*/)
var db = pgp('postgres://cym:cym@mdmcloud.tobeway.com:5432/postgres')

/* GET users listing. */

router.get('/:classID', function(req, res, next) {
    var q = '';
    q += 'SELECT C."ClassID", C."ClassName_EN", C."ClassType", P."PropName_EN", CP."DefPropVal", P."PropType" from  cym."Class" C ';
    q += 'INNER JOIN cym."ClassProp" CP ON C."ClassID" = CP."ClassID" ';
    q += 'INNER JOIN cym."Prop" P ON CP."PropID" = P."PropID" ';
    q += 'WHERE C."ClassID" = $1 ';
    q += 'AND C."Active" = \'Y\' AND P."Active" = \'Y\'';
    q += 'ORDER BY CP."DispSeq" ';
    db.any(q, req.params.classID)
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
