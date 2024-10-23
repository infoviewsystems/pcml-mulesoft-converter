var xml;
document.getElementById("middlediv").style.display = "none";
document.getElementById("bottomdiv").style.display = "none";

let convertToProgramCallDef = () =>{
xml=document.getElementById("inputXML").value;
    $.ajax({
        type: 'POST',
        url: "http://localhost:8080/api/from-string",
        data: xml,
        success: function (res) {
            var node = document.getElementById('outputXML');
            node.innerText = res;
            document.getElementById("middlediv").style.display = "block";
            convertToRaml(res);
        },
        error: function (e) {
            console.log(e);
        },
        contentType: "text/plain;charset=UTF-8"
    });
}

$("#convert").on("click", () =>{
xml=document.getElementById("inputXML").value;
if(xml.length > 4){
  convertToProgramCallDef()
}else{
alert("Please Enter The Input!!");
}
})



$("#middledivcopy").on("click", () =>{
var programDef=document.getElementById("outputXML");
var range = document.createRange();
range.selectNode(programDef);
window.getSelection().removeAllRanges();
window.getSelection().addRange(range);
document.execCommand('copy');
window.getSelection().removeAllRanges();
})

$("#bottomdivcopy").on("click", () =>{
var programDef=document.getElementById("outputXMLR");
var range = document.createRange();
range.selectNode(programDef);
window.getSelection().removeAllRanges();
window.getSelection().addRange(range);
document.execCommand('copy');
window.getSelection().removeAllRanges();
})

let convertToRaml = (raml) =>{
    $.ajax({
        type: 'POST',
        url: "http://localhost:8080/api/from-string-to-raml",
        data: raml,
        success: function (res) {
            var node = document.getElementById('outputXMLR');
            node.innerText = res;
            document.getElementById("bottomdiv").style.display = "block";
        },
        error: function (e) {
            console.log(e);
        },
        contentType: "text/plain;charset=UTF-8"
    });
}
