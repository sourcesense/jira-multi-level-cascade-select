var masterSlaveMap = {};
var slaveOptionsMap = {};

function dynamicMultiLevelSelect(masterId, slaveId) {
    var masterSelector = "select#" + masterId;
    var slaveSelector = "select#" + slaveId;
    masterSlaveMap[masterSelector] = slaveSelector;
jQuery(document).ready(function($) {
    // Code that uses jQuery's $ can follow here.

    var slaveOptions = $("option", slaveSelector);
    slaveOptionsMap[slaveSelector] = slaveOptions;
    $(masterSelector).change(
        function(event) {
            $(slaveSelector).hide();
            $(slaveSelector).empty();
            var options = slaveOptions.filter("."+event.target.value);
            options.filter("[value='-1']").each(
                function() {
                    $(slaveSelector).append(this);
                }
            );
            slaveOptions.filter(".select").each(
                function() {
                    $(slaveSelector).append(this);
                }
            );
            options.filter("[value!='-1']").each(
                function() {
                    $(slaveSelector).append(this);
                }
            );

            $(slaveSelector).show();
            clickFirstVisible(slaveSelector);
        }
    );
  });
}

function clickFirstVisible(select) {
jQuery(document).ready(function($) {
    // Code that uses jQuery's $ can follow here.
	$("option:first", select).each(
		function() {
			$(select).attr("selectedIndex", this.index);
			$(select).trigger("change");
		}
	);
  });
}

function selectOption(select, value) {
jQuery(document).ready(function($) {
    var option = $("option[value='"+value+"']", select);
    if (option.size()>0) {
        $(select).attr("selectedIndex", option.get(0).index);
        while (select in masterSlaveMap) {
            select = masterSlaveMap[select];

            // selectOption is only called on the page load.  We don't
            // want to trigger anything on page load, since we can't
            // guarrantee that things will get executed in the correct
            // order.  So we crib code from dynamicMultiLevelSelect
            // but we use a loop instead of depending on the change event.

            var slaveOptions = slaveOptionsMap[select];
            $(select).hide();
            $(select).empty();
            var options = slaveOptions.filter("."+value);
            options.filter("[value='-1']").each(
                function() {
                    $(select).append(this);
                }
            );
            slaveOptions.filter(".select").each(
                function() {
                    $(select).append(this);
                }
            );
            options.filter("[value!='-1']").each(
                function() {
                    $(select).append(this);
                }
            );

            $(select).show();
            $(select).attr("selectedIndex", 0);
            value = $(select).val();
        }
    }
  });
}
