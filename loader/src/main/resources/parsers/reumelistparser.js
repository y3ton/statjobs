findObj = (selector, element) => {
   	return element ? Array.prototype.slice.call(element.querySelectorAll(selector)) : [];
}

findText = (selector, element) => {
    var e = element ? Array.prototype.slice.call(element.querySelectorAll(selector))[0] : "";
    return e ? e.innerText : "";
}

findResumes = () => findObj('[data-qa="resume-serp__resume"]', document).map(element => {
    return [findObj('[data-qa="resume-serp__resume-title"]', element)[0].href, findText('[class="output__tab m-output__date"]', element)]
})