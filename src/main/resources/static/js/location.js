$(document).ready (function()
        {
            $.get("https://api.ipify.org?format=json", function(data)
            {
                $("#ip").val(data.ip);
            });

            function showLocationOnMap(location)
            {
                var map;
                map = new google.maps.Map(document.getElementById('map'),
                {
                    center: {lat: Number(location.latitude), lng: Number(location.longitude)},
                    map: map, title: "Public IP:"+location.ipAddress+" @ " + location.city
                });
            }

            $("#ipForm").submit(function(event)
            {
                event.preventDefault();
                $.ajax(
                {
                    url : "console",
                    type : "POST",
                    contentType : "application/x-www-form-urlencoded; charset=UTF-8",
                    data : $.param({ipAddress : $("#ip").val()}),

                    success : function(data)
                    {
                        $("#status").html(JSON.stringify(data));
                        if(data.ipAddress != null)
                        {
                            showLocationOnMap(data);
                        }
                    },

                    error : function(err)
                    {
                        $("#status").html("Error : " + JSON.stringify(data));
                    }
                });
            });
        });