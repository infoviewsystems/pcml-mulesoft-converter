var express = require('express');
var router = express.Router();


/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/form-object', function (req,res, next){
  res.render('index', { obj : '<test>Test</test>'});
});

module.exports = router;
