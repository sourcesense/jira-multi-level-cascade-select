function dynamicMultiLevelSelect(masterId, slaveId) {
    var masterSelector = "select#" + masterId;
    var slaveSelector = "select#" + slaveId;
jQuery(document).ready(function($) {
    // Code that uses jQuery's $ can follow here.

    var slaveOptions = $("option", slaveSelector);
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
        $(select).trigger("change");
    } else {
        clickFirstVisible(select);
    }
  });
}