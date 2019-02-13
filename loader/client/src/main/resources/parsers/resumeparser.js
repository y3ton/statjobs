findTextContains = (selector, element, text) => {
	if (element) {
		var e = Array.prototype.slice.call(element.querySelectorAll(selector)).map(e => e.innerText).filter(s => s.indexOf(text) == 0)[0];
		return e ? e.replace(text, "").trim(): "";
	} else {
		return "";
	}
}

findObj = (selector, element) => {
	return element ? Array.prototype.slice.call(element.querySelectorAll(selector)) : [];
}

findText = (selector, element) => {
	var e = element ? Array.prototype.slice.call(element.querySelectorAll(selector))[0] : "";
	return e ? e.innerText : "";
}

verify = (obj) => {
	var text = findText('[class="resume-wrapper"]', document);
	var json = JSON.stringify(obj);
	var allow = ["Обо", "мне", "Занятость", "График", "языков", "Повышение", "квалификации", "Опыт", "Ключевые", "навыки", "Знание", "курсы", "Портфолио", "Тесты", "экзамены", "Результаты", "теста", "Профориентация", "посмотреть",
	             "Employment", "Key", "About", "Languages", "Professional", "courses", "Tests", "examinations", "Work", "experience", "development", "Proforinetation", "results", "view", "Portfolio", "skills"];
	return text.replace(/;|:|,|\n|\t|«|»|["']|\\|\(\)/g, " ").replace(/\u0009/g, " ").replace(/\u00a0/g, " ")
	.split(" ")
	.filter(item => item.length > 1)
	.filter(item => json.indexOf(item) < 1)
	.filter(item => allow.indexOf(item) < 0)
}

parseResume = () => {
    var o = {
	gender: 			findText('[data-qa="resume-personal-gender"]', document),
	age: 				findText('[data-qa="resume-personal-age"]', document),
	birthday: 			findObj('[data-qa="resume-personal-birthday"]', document)[0] ? findObj('[data-qa="resume-personal-birthday"]', document)[0].getAttribute("content") : "",
	address: 			findText('[itemprop="address"]', document),
	metro: 				findText('[data-qa="resume-personal-metro"]', document),
	position: 			findText('[data-qa="resume-block-title-position"]', document),
	salary: 			findText('[data-qa="resume-block-salary"]', document),
	specializationC: 	findText('[data-qa="resume-block-specialization-category"]', document),
	specializationP: 	findObj('[data-qa="resume-block-position-specialization"]', document).map(e => e.innerText),
	employment: 		(findTextContains('p',findObj('[class="resume-block"]', document)[0], "Занятость: ") + " " +
                         findTextContains('p',findObj('[class="resume-block"]', document)[0], "Employment: ")).trim(),
	schedule: 			(findTextContains('p',findObj('[class="resume-block"]', document)[0], "График работы: ") + " " +
                         findTextContains('p',findObj('[class="resume-block"]', document)[0], "Work schedule: ")).trim(),
	other: 				(findTextContains('[class="resume-block"]', document, "Citizenship, travel time to work") + " " +
                         findTextContains('[class="resume-block"]', document, "Гражданство, время в пути до работы")).trim(),
	experience: 		(findTextContains('[class="bloko-columns-row"]', document, "Опыт работы ") + " " +
                         findTextContains('[class="bloko-columns-row"]', document, "Work experience ")).trim(),

	language: 			findObj('[data-qa="resume-block-language-item"]', document).map(item => item.innerText),
	skils: 				findObj('[data-qa="bloko-tag__text"]', document).map(item => item.innerText),
	about: 				findText('[data-qa="resume-block-skills"]', document),
	driver: 			findText('[data-qa="resume-block-driver-experience"]', document),

	educationGrade: 	findText('[class="bloko-columns-row"]', (findObj('[data-qa="resume-block-education"]', document)[0])),

	experiencedetail: findObj('[itemprop="worksFor"]', document).map(item => {
                return {
                               period:      findObj( '[class="bloko-column bloko-column_xs-4 bloko-column_s-2 bloko-column_m-2 bloko-column_l-2"]', item)[0].innerText.split("\n")[0],
                               duration:    findObj( '[class="resume-block__experience-timeinterval"]', item)[0].innerText,
                               company:     findText('[class="resume-block__sub-title"]', item),
                               address:     findText('[itemprop="address"]', item),
                               url:         findText('[class="resume__experience-url"]', item),
                               field:       findText('[class="resume-block__experience-gap-bottom"]', item),
                               position:    findText('[data-qa="resume-block-experience-position"]', item),
                               description: findText('[data-qa="resume-block-experience-description"]', item)
                };
	}),

	educationadd: findObj('[class="resume-block-item-gap"]', findObj('[data-qa="resume-block-additional-education"]', document)[0])
		.map(item => {
			return {
				year : findText('[class="bloko-column bloko-column_xs-4 bloko-column_s-2 bloko-column_m-2 bloko-column_l-2"]', item),
				name : findText('[data-qa="resume-block-education-name"]', item),
				organization : findText('[data-qa="resume-block-education-organization"]', item),
			}
		}).filter(item => item.name),

	education: findObj('[class="bloko-columns-row"]', findObj('[data-qa="resume-block-education"]', document)[0])
		.map(item => {
			return {
				year: findText('[class="bloko-column bloko-column_s-2 bloko-column_m-2 bloko-column_l-2"]', item),
				name: findText('[data-qa="resume-block-education-name"]', item),
				organization: findText('[data-qa="resume-block-education-organization"]', item)
			}
		}).filter(item => item.name),

	educationexam: findObj('[class="bloko-columns-row"]', findObj('[data-qa="resume-block-attestation-education"]', document)[0])
		.map(item => {
			return {
				year: findText('[class="bloko-column bloko-column_s-2 bloko-column_m-2 bloko-column_l-2"]', item),
				name: findText('[data-qa="resume-block-education-name"]', item),
				organization: findText('[data-qa="resume-block-education-organization"]', item)
			}
		}).filter(item => item.name)
    }
    o.err = verify(o);
    return JSON.stringify(o);
}