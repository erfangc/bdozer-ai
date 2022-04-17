const {convert} = require('html-to-text');
const express = require('express',);
const port = 3000;
const app = express();
const bodyParser = require('body-parser');

app.use(bodyParser.text({type: 'text/plain', limit: '5mb'}));

app.post('/convert', (req, resp) => {
    console.log(`Handling html Conversion`);
    const html = req.body;
    const text = convert(html, { wordwrap: 120 });
    resp.end(text);
})

app.listen(port, () => {
    console.log(`Server listening on ${port}`);
});
