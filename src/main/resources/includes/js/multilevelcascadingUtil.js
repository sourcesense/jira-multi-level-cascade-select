var masterSlaveMap = {};
var slaveOptionsMap = {};

function dynamicMultiLevelSelect(masterId, slaveId) {
	console.log("Dynamic Script: Ingresso P:"+masterId+" C :"+slaveId)
    var masterSelector = "select#" + masterId; // select list padre
    var slaveSelector = "select#" + slaveId;  // select list figlio
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
// select è il selettore della select list
// value è l'id della option da selezionare
function selectOption(select, value, pid) {
jQuery(document).ready(function($) {
	console.log(" Select : "+value+"in Select list:"+select+" with this pid:"+pid)
    var option = $("option[value='"+value+"']", select);// seleziona l'opzione nella lista con il valore prescelto
	var optionSibling = $("option[class='"+pid+"']", select);
    if (option.size()>0) {// se la trova
        $(select).attr("selectedIndex", option.get(0).index);// aggiunge al selettore l'attributo selectedIndex con l'indice della opzione scelt
    /*
        questo loop cicla sulle select rimanenti e le valorizza soltanto con i valori coerenti con la precedente scelta 
        */ 
        console.log("Master Slave Map"+masterSlaveMap)
        while (select in masterSlaveMap) {
            select = masterSlaveMap[select]; // mi prendo la select

            // selectOption is only called on the page load.  We don't
            // want to trigger anything on page load, since we can't
            // guarrantee that things will get executed in the correct
            // order.  So we crib code from dynamicMultiLevelSelect
            // but we use a loop instead of depending on the change event.

            var slaveOptions = slaveOptionsMap[select];// prendo tutte le opzioni di questa select dalla mappa
            console.log("in Select list:"+select+" retrieved:"+slaveOptions.size()+" options")
            $(select).hide();
            $(select).empty();
            console.log("Start filter on: "+value)
            var options = slaveOptions.filter("[class="+value+"]");//filtra le option di un livello sul valore del padre scelto
             console.log("Options filtered "+options.size())
            
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
			
            $(select).show();// lo mostra e resetta l'indice 
            $(select).attr("selectedIndex", 0);
            value = $(select).val();
        }
    }
  });
}
