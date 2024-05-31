document.addEventListener('DOMContentLoaded', function () {
    function updateSize() {
        var customFile = document.getElementById("customFile").files[0];
        var permissionFile = document.getElementById("permissionFile").files[0];
        var sizeInMB = ((permissionFile.size + customFile.size) / (1024 * 1024)).toFixed(2);
        var sizePermissionFile = (permissionFile.size / (1024 * 1024)).toFixed(2);
        if (sizePermissionFile < 0.01) {
            document.getElementById('submitButton').setAttribute('disabled', 'disabled')
            document.getElementById("fileInfo").innerHTML = [
                "<b>Необходимо загрузить заполненный файл разрешения публикации!</b>",
            ].join("<br>");
        } else {
            if (sizeInMB > 10) {
                document.getElementById('submitButton').setAttribute('disabled', 'disabled')
                document.getElementById("fileInfo").innerHTML = [
                    "<b>Общий размер файлов превышает допустимый лимит в 10MB: " + sizeInMB + " MB</b>",
                ].join("<br>");
            } else {
                document.getElementById('submitButton').removeAttribute('disabled')
                document.getElementById("fileInfo").innerHTML = [
                    "Общий размер файлов: " + sizeInMB + " MB",
                ].join("<br>");
            }
        }
    }

    document.getElementById('customFile').addEventListener('change', updateSize);
    document.getElementById('permissionFile').addEventListener('change', updateSize);
});

$('textarea').on("input", function () {
    const maxlength = $(this).attr("maxlength");
    const currentLength = $(this).val().length;
    $(this).siblings('.chars-current').html(maxlength - currentLength);
    $(this).siblings('.chars-total').html(maxlength);
});

$(document).ready(function () {
    $('[repeat]').each(function () {
        var toRepeat = $(this).text();
        var times = parseInt($(this).attr('repeat'));
        var repeated = Array(times + 1).join(toRepeat);
        $(this).text(repeated).removeAttr('repeat');
    });
});

$('.btn.btn-primary.load').click(function () {
    $(this).html('<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Загрузка...')
});

function sourceName(request, response) {
    response($.map(mydata, function (obj, key) {
        var name = obj.lastName.toUpperCase();
        var organization = obj.organizationShort;
        if (name.indexOf(request.term.toUpperCase()) !== -1) {
            if (organization !== null) {
                return {
                    label: obj.lastName + " " + obj.firstName[0] + "." + obj.middleName[0] + ". (" + obj.organizationShort + ", " + obj.city + ")", // Label for Display
                    lastName: obj.lastName, // Value
                    firstName: obj.firstName, // Value
                    middleName: obj.middleName, // Value
                    organizationShort: obj.organizationShort, // Value
                    city: obj.city // Value
                }
            } else {
                return {
                    label: obj.lastName + " " + obj.firstName[0] + "." + obj.middleName[0] + ". ", // Label for Display
                    lastName: obj.lastName, // Value
                    firstName: obj.firstName, // Value
                    middleName: obj.middleName, // Value
                    organizationShort: 'Организация'
                }
            }
        } else {
            return null;
        }
    }));
}

function sourceOrganization(request, response) {
    response($.map(jsonAffiliationSet, function (obj, key) {
        var name = obj.organizationShort.toUpperCase();
        if (name.indexOf(organization.toUpperCase()) !== -1) {
            return {
                label: obj.organizationShort + ", " + obj.city, // Label for Display
                organizationShort: obj.organizationShort, // Value
                city: obj.city // Value
            }
        } else {
            return null;
        }
    }));
}

$('#lastName0').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort0").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort0").style.borderColor = "#D3D3D3"
        }
        $('#lastName0').val(ui.item.lastName);
        $('#firstName0').val(ui.item.firstName);
        $('#middleName0').val(ui.item.middleName);
        $('#organizationShort0').val(ui.item.organizationShort);
        $('#city0').val(ui.item.city);
    }
});

$('#organizationShort0').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort0").style.borderColor = "#D3D3D3"
        }
        $('#city0').val(ui.item.city);
    }
});

$('#lastName1').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort1").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort1").style.borderColor = "#D3D3D3"
        }
        $('#lastName1').val(ui.item.lastName);
        $('#firstName1').val(ui.item.firstName);
        $('#middleName1').val(ui.item.middleName);
        $('#organizationShort1').val(ui.item.organizationShort);
        $('#city1').val(ui.item.city);
    }
});

$('#organizationShort1').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort1").style.borderColor = "#D3D3D3"
        }
        $('#city1').val(ui.item.city);
    }
});

$('#lastName2').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort2").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort2").style.borderColor = "#D3D3D3"
        }
        $('#lastName2').val(ui.item.lastName);
        $('#firstName2').val(ui.item.firstName);
        $('#middleName2').val(ui.item.middleName);
        $('#organizationShort2').val(ui.item.organizationShort);
        $('#city2').val(ui.item.city);
    }
});

$('#organizationShort2').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort2").style.borderColor = "#D3D3D3"
        }
        $('#city2').val(ui.item.city);
    }
});

$('#lastName3').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort3").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort3").style.borderColor = "#D3D3D3"
        }
        $('#lastName3').val(ui.item.lastName);
        $('#firstName3').val(ui.item.firstName);
        $('#middleName3').val(ui.item.middleName);
        $('#organizationShort3').val(ui.item.organizationShort);
        $('#city3').val(ui.item.city);
    }
});

$('#organizationShort3').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort3").style.borderColor = "#D3D3D3"
        }
        $('#city3').val(ui.item.city);
    }
});

$('#lastName4').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort4").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort4").style.borderColor = "#D3D3D3"
        }
        $('#lastName4').val(ui.item.lastName);
        $('#firstName4').val(ui.item.firstName);
        $('#middleName4').val(ui.item.middleName);
        $('#organizationShort4').val(ui.item.organizationShort);
        $('#city4').val(ui.item.city);
    }
});

$('#organizationShort4').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort4").style.borderColor = "#D3D3D3"
        }
        $('#city4').val(ui.item.city);
    }
});

$('#lastName5').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort5").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort5").style.borderColor = "#D3D3D3"
        }
        $('#lastName5').val(ui.item.lastName);
        $('#firstName5').val(ui.item.firstName);
        $('#middleName5').val(ui.item.middleName);
        $('#organizationShort5').val(ui.item.organizationShort);
        $('#city5').val(ui.item.city);
    }
});

$('#organizationShort5').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort5").style.borderColor = "#D3D3D3"
        }
        $('#city5').val(ui.item.city);
    }
});

$('#lastName6').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort6").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort6").style.borderColor = "#D3D3D3"
        }
        $('#lastName6').val(ui.item.lastName);
        $('#firstName6').val(ui.item.firstName);
        $('#middleName6').val(ui.item.middleName);
        $('#organizationShort6').val(ui.item.organizationShort);
        $('#city6').val(ui.item.city);
    }
});

$('#organizationShort6').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort6").style.borderColor = "#D3D3D3"
        }
        $('#city6').val(ui.item.city);
    }
});

$('#lastName7').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort7").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort7").style.borderColor = "#D3D3D3"
        }
        $('#lastName7').val(ui.item.lastName);
        $('#firstName7').val(ui.item.firstName);
        $('#middleName7').val(ui.item.middleName);
        $('#organizationShort7').val(ui.item.organizationShort);
        $('#city7').val(ui.item.city);
    }
});

$('#organizationShort7').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort7").style.borderColor = "#D3D3D3"
        }
        $('#city7').val(ui.item.city);
    }
});

$('#lastName8').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort8").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort8").style.borderColor = "#D3D3D3"
        }
        $('#lastName8').val(ui.item.lastName);
        $('#firstName8').val(ui.item.firstName);
        $('#middleName8').val(ui.item.middleName);
        $('#organizationShort8').val(ui.item.organizationShort);
        $('#city8').val(ui.item.city);
    }
});

$('#organizationShort8').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort8").style.borderColor = "#D3D3D3"
        }
        $('#city8').val(ui.item.city);
    }
});

$('#lastName9').autocomplete({
    minLength: 3,
    source: sourceName,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort === "Организация") {
            document.getElementById("organizationShort9").style.borderColor = "#383838"
        } else {
            document.getElementById("organizationShort9").style.borderColor = "#D3D3D3"
        }
        $('#lastName9').val(ui.item.lastName);
        $('#firstName9').val(ui.item.firstName);
        $('#middleName9').val(ui.item.middleName);
        $('#organizationShort9').val(ui.item.organizationShort);
        $('#city9').val(ui.item.city);
    }
});

$('#organizationShort9').bind('change', function () {
    organization = this.value
}).autocomplete({
    source: sourceOrganization,
    select: function (event, ui) {
        event.preventDefault();
        if (ui.item.organizationShort !== "Организация") {
            document.getElementById("organizationShort9").style.borderColor = "#D3D3D3"
        }
        $('#city9').val(ui.item.city);
    }
});

$('#organizationFull').autocomplete({
    minLength: 2,
    source: function (request, response) {
        response($.map(jsonAffiliationSet, function (obj, key) {
            var name = obj.organizationFull.toUpperCase();
            const text = null;
            if (name.indexOf(request.term.toUpperCase()) !== -1) {
                return {
                    label: obj.organizationFull, // Label for Display
                    organizationFull: obj.organizationFull, // Value
                    organizationShort: obj.organizationShort, // Value
                    city: obj.city, // Value
                    country: obj.country, // Value
                }
            } else {
                return null;
            }
        }));
    },
    select: function (event, ui) {
        event.preventDefault();
        $('#organizationFull').val(ui.item.organizationFull);
        $('#organizationShort').val(ui.item.organizationShort);
        $('#city').val(ui.item.city);
        $('#country').val(ui.item.country);
        text.innerHTML = "Данная организация присутствует в списке!";
    }
});

document.addEventListener('DOMContentLoaded', function () {
    let section = document.querySelector('#section')
    let type = document.querySelector('#type')
    let publicationName = document.getElementById('publicationName')

    document.getElementById("section").style.borderColor = "#383838"
    document.getElementById("type").style.borderColor = "#383838"
    document.getElementById("publicationName").style.borderColor = "#383838"

    section.addEventListener('change', function () {
        if (section.value === 'ВЫБЕРИТЕ СЕКЦИЮ*') {
            document.getElementById("section").style.borderColor = "#383838"
        } else {
            document.getElementById("section").style.borderColor = "#D3D3D3"
        }
    });
    type.addEventListener('change', function () {
        if (type.value === 'ВЫБЕРИТЕ ТИП ДОКЛАДА*') {
            document.getElementById("type").style.borderColor = "#383838"
        } else {
            document.getElementById("type").style.borderColor = "#D3D3D3"
        }
    });
    publicationName.addEventListener('change', function () {
        if (publicationName === null || publicationName.value === '') {
            document.getElementById("publicationName").style.borderColor = "#383838"
        } else {
            document.getElementById("publicationName").style.borderColor = "#D3D3D3"
        }
    });
})

document.addEventListener('DOMContentLoaded', () => {
    document.querySelector('#lastName0').value = jsonCurrentUser.lastName;
    document.querySelector('#firstName0').value = jsonCurrentUser.firstName;
    document.querySelector('#middleName0').value = jsonCurrentUser.middleName;
    if (jsonCurrentUser.organizationShort != null) {
        document.querySelector('#organizationShort0').value = jsonCurrentUser.organizationShort;
        document.querySelector('#city0').value = jsonCurrentUser.city;
    } else {
        document.getElementById("organizationShort0").style.borderColor = "#383838"
    }
});
