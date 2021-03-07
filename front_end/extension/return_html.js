chrome.runtime.onMessage.addListener(
    function(request, sender, sendResponse) {
        if (request.msg == "html_request"){
            const tabId = request.id;
            raw_html = document.getElementsByTagName('html')[0].innerHTML;
            raw_html = String(raw_html);
            console.log("BAM ", raw_html);
            sendResponse({answer:"fulfilled"});
            setTimeout(function(){ chrome.runtime.sendMessage({html: raw_html, type: "html"}); }, 10);
            return true;
        }
        if (request.msg == "update_tab"){
            return true;
        }
    }
);