/*global AJS */
var MLCS = (function ($) {
    function dynamicSelect(parentId, childId) {
        var parentSelector = "select#" + parentId,
            childSelector = "select#" + childId;

        $(document).ready(function () {
            var childOptions = $("option", childSelector),
                child = $(childSelector);

            $(parentSelector).change(function (event) {
                child.hide();
                child.empty();

                var options = childOptions.filter("." + event.target.value);
                options.filter("[value='-1']").each(function () {
                    child.append(this);
                });
                childOptions.filter(".select").each(function () {
                    child.append(this);
                });
                options.filter("[value!='-1']").each(function () {
                    child.append(this);
                });

                child.val("-1");
                child.trigger("change");
                child.show();
            });
        });
    }

    function selectOption(selector, value) {
        $(document).ready(function () {
            $(selector).val(value);
            $(selector).trigger('change');
        });
    }

    return {
        dynamicSelect: dynamicSelect,
        selectOption: selectOption
    };
}(AJS.$));

