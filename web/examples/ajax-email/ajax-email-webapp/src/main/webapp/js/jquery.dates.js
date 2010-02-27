/*
  Version       :    0.01
  Release Date  :    5 June 2007
  Author        :    Shawn Grover, jqdates@open2space.com
  License       :     
      This library is made available to the general public under the 
      Creative Commons Attribution-ShareAlike 2.5 Canada license.
      (http://creativecommons.org/licenses/by-sa/2.5/ca/)
      
      In short, use it as you will, but please do not claim the work as your own.
      
  Library Description:
      This library aims to add more robust date functionality to Javascript.
      The goal is to allow developers to use dates in a natural way, without 
      worrying about what format a date may be in, or the underlying math for 
      the date calculations.
  
  Credits:
      The parseDate() method is heavily based on Matt Kruse's logic within 
      his dates library.  ( http://www.mattkruse.com/javascript/date/ )
      Matt's work is licensed under the GPL.  If you use or modify the 
      parseDate routine for your own puposes, please give Matt the credit
      he is due.
      
      The date picker drawing routines are based on code found at
      http://jszen.blogspot.com/2007/03/how-to-build-simple-calendar-with.html
      I do not see any license or copyright notice there at this time 
      (I could be blind).  But this blogger deserves credit for their posting
      which lead me to the code contained herein.
      
      All other code was written from scratch by Shawn Grover, based on 
      accumulated experience with Javascript and dates.


  Usage:
      - Only two global variables are used for the routine.  The names have 
        been chosen to avoid likely name conflicts with other libraries.
      - All the code, with the exception of the two global variables and the
        jQuery plugin code is contained within a single object called "o2s".  
        This has been done to emulate a namespace and minimize name conflicts.
  
      - Include your libraries:
          <script type="text/javascript" src="jquery.js"></script>
          <script type="text/javascript" src="jquery.dates.js"></script>
          
      - After the libraries are included, you have access to the functions.
       
      DATE PICKER
      ===========
        The date picker calendar will appear directly below the element it 
        is associated with.
        
        Possible Options:
            {
              //the date format to use for the selected date
              //the format string is based on the formatting function defined below
              format    : "yyyy-mm-dd",
              
              //what date should be highlight by default when the date picker
              //initially becomes visible?
              //if null or not set, defaults to the date in the text box, 
              //or the current date if the text box does not contain a date
              date      :   new Date(),
              
              //should a different form element be update
              //used when adding an anchor, image, or text that will trigger
              //the date picker, but the date should be stored somewhere else
              //once selected
              //- the referenced object must be a DOM element.
              parent    :   object
            }
          
        Samples
          
          - adding a datepicker to a textbox, with default settings:
                $("#mytextboxID").datePicker();
            
          - adding a datepicker to an anchor tag, but updating a text box
                $("#myanchorID").datePicker({parent : $("#mytextboxID")[0] });
                
      IS DATE
      =======
         Often we need to know if a string of text is a valid date.  This method
         returns true/false if the text string can be seen as a date.
         
         Usage
          o2s.isDate("datestring", formatstring);
          
          $("#mytextboxID").isDate(formatstring);
              
              **  note the jQuery .dateAdd() does not return a
              jQuery object.
              
        The formatstring parameter is optional.  It is the same type of format 
        string used in the format() and parseDate() methods below.
        
         
      DATE FORMAT
      ===========
        The date format function can be called directly via:
        
            o2s.format(date, format);
            
        OR a jQuery method is available for use with text boxes that contain a date:
        
            $("#mytextboxID").dateFormat("ddd, dd mmmm yyyy");
            
            - this will make sure the value within the textbox is formatted to the 
              desired structure, if possible.
              
        The format string uses the following tokens to determine what values to insert:

Formatting Tokens
  d     - day of month (not padded)       h     - hour, 12 hour clock (not padded)
  dd    - day of month (zero padded)      hh    - hour, 12 hour clock (zero padded)
  ddd   - day name, abbreviated           H     - hour, 24 hour clock (not padded)
  dddd  - day name, full                  HH    - hour, 24 hour clock (zero padded)
  m     - month number (not padded)       i     - minutes (not padded)
  mm    - month number (zero padded)      ii    - minutes (zero padded)
  mmm   - month name, abbreviated         s     - seconds (not padded)
  mmmm  - month name, full                ss    - seconds (zero padded)
  y     - two digit year (not padded)     a     - am / pm (lowercase)
  yy    - two digit year (zero padded)    A     - AM / PM (uppercase)
  yyyy  - four digit year                 O     - timze zone offset from GMT.  
  j     - ordinal day/julian date                 (that is a letter OH, not zero)
  jjj   - ordinal day/julian date (same as "j")
  
Prebuilt formats
  short       - "m/d/yy"              
  medium      - "mmm d yyyy"          
  long        - "mmmm d yyyy"         
  shorttime   - "m/d/yy HH:ii"        
  mediumtime  - "mmm d yyyy HH:ii"    
  longtime    - "mmmm d yyyy hh:ii:ss A O"
  iso8610     - "yyyy-mm-ddTHH:ii:ss O"
  yyyymmdd    - "yyyy-mm-dd"

        Any text in the formatting string that is not one of the tokens above,
        will be inserted into the output directly.
        
        Examples:     (assuming a date of 1 Jun 2007 15:05:30)
        
        "dddd, dd mmmm yyyy"    =>    "Friday, 01 June 2007"
        "d : mmm : yy"          =>    "1 : Jun : 07"
        "shorttime"             =>    "1/7/07 15:05"
        "hh:ii a = HH:ii"       =>    "03:05 pm = 15:05"
        
    PARSE DATE
    ==========
      The parseDate function attempts to extract a date object from an arbitrary 
      string.  A format string can be specified to facilitate a quicker check.
      If the format string is omitted, the formats found in the commonformats
      array will be tried until a valid date object is found, or the full list 
      has been completed.
      
      NOTE: It is possible for a date string to match more than one date format.
      In this event you will see odd dates appear.  Specify a format string to 
      minimize these odd behaviors, or thoroughly test the formats you are 
      expecting to make sure the desired behavior is seen.  If needed, 
      modify the commonformats array to suit your needs.
      
      The format string uses the same tokens for the format method described above.
      
      If a date object cannot be determined, a null value is returned
      
      Usage:
        standard javascript:
            var d = o2s.parseDate(date, format);
            
        jQuery
            var d = $("#mytextboxID").parseDate(format);
            
    DATE DIFF
    =========
      This function determines the quantity of a given unit between two dates.
      For instance, how many hours between now and chirstmas, how many years
      between events, how many hours from the start time to now (i.e. duration)
    
      Units:  ms  - milliseconds      d - days
              s   - seconds           w - weeks
              i   - minutes           y - years
              h   - hours
      
      Usage:  
        standard Javascript
          var d = o2s.dateDiff("unit",startdate, enddate);
          
        jQuery
          var d = $("#mytextboxID").dateDiff("unit", date);
          
          **  note the jQuery .dateDiff() does not return a
              jQuery object.
          
    DATE ADD
    ========
      This function adds a specified number of units to a date, to derive 
      a new date.  (i.e. add 3 weeks to the current date, what is the date 45 
      days after chirstmas, etc.)
    
      Units:  ms  - milliseconds      d - days
              s   - seconds           w - weeks
              i   - minutes           y - years
              h   - hours
      
      Usage:  
        standard Javascript
          var d = o2s.dateAdd("unit",qty, date);
          
        jQuery
          var d = $("#mytextboxID").dateAdd("unit", qty);
    
              **  note the jQuery .dateAdd() does not return a
              jQuery object.

        
*/

o2sCalObject = null;    //track which calendar item is to be displayed
o2sCalShowTime = false; //indicate if the time part of the popup should be visible.

var o2s = {
    //********************************
    //Configuration Variables
    //********************************
    popupTextLastMonth  :   "&lt;&lt;",
    popupTextLastYear   :   "&nbsp;&lt;&nbsp;",
    popupTextNextMonth  :   "&gt;&gt;",
    popupTextNextYear   :   "&nbsp;&gt;&nbsp;",
    popupTextToday      :   "Today",
    
    //Calendar CSS classes
    classCal            :   "o2sCalTable",           //The calendar table
    classCalHeader      :   "o2sCalHeader",          //The day name header row
    classCalHeaderDay   :   "o2sCalHeaderDay",      //The day names themselves
    classCalDay         :   "o2sCalDay",             //A regular day in the calendar
    classCalDayTarget   :   "o2sCalDayTarget",      //The target/default date
    classCalHover       :   "o2sCalHover",            //the class when a day is being hovered over.
    
    //********************************
    // Date collections and values
    // arrays of date related items.
    //********************************
    dayletter     : [ "S", "M", "T", "W", "T", "F", "S" ],
    dayshort      : [ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" ],
    daylong       : [ "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" ],
    monthshort    : ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ],
    monthlong     : ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
    monthdays     : [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],
    millisecond   : 1,
    second        : 1000,                 //1000 milliseconds
    minute        : 1000 * 60,     // 60 seconds
    hour          : 1000 * 60 * 60,     // 60 minutes
    day           : 1000 * 60 * 60 * 24,       // 24 hours
    week          : 1000 * 60 * 60 * 24 * 7,          // 7 days
    year          : 1000 * 60 * 60 * 24 * 365,        // 365 days
    
    
    //the common formats are used to aide in parsing a date string when a format is not known.
    //NOTE: One date string may match more than one format.  If abnormal behavior is observed, specify a format when using the parseDate() method,
    //      or using an function that relies on it (such as the date picker).
    commonformats : [
                      //little endian
                      "d/m/yyyy", "dd/mm/yyyy", "dd-mm-yyyy", "dd-mm-yy", "d mmmm yyyy", "dd mmmm yyyy", "dd mmmm yy", "d mmmm yy", 
                      "dd.mm.yyyy", "d.m.yyyy", "d.m.y", "d. mmmm yy", "d. mmmm yyyy",
                      "d mmm yyyy", "dd mmm yyyy", "d mmm yy", "dd mmm yy",
                      
                      //big endian
                      "yyyy mmmm dd", "yy mmmm dd", "yyyy mmmm d", "yy mmmm d", "yyyy-mm-dd", "yyyy.mm.dd",
                      
                      //middle endian
                      "mmmm d, yyyy", "mmmm dd, yyyy", "mmmm d, yy", "mmmm dd, yy", "mmm d, yy", "mmm d, yyyy", "mmm dd, yy",
                      "mmm. d, yyyy", "mmm. dd, yyyy", "mmm. d, yy", "mmm. dd, yy", "mm/dd/yyyy", "mm-dd-yyyy", "mm.dd.yy",
                      "m/d/yy", "mm/dd/yy", "mm/dd/yyyy",
                      
                      //weekday names
                      "ddd, dd mmm yyyy",
                      "ddd mmm dd yyyy HH:ii:ss",   //layout from javascript date().toString()
                      
                      //julian date
                      "yyyy-j", "yyyy-jjj", "yyyyjjjj", "yyyyj"
                    ],
                      
                      
    //********************************
    //Misc utility methods
    //********************************
    
    // ***********************************************
    //   PAD
    //  
    //  zero pads a number if it is less than 10.
    //    Usage:  o2s.padd(number);
    //
    //  Returns a string
    // ***********************************************
    pad       :   function (x) { return (x<0||x>9?"":"0") + x; },
    
    // ***********************************************
    //   GET TOKENS
    //  
    //  Finds similar, consecutive items in a string.
    //  (i.e. "aaaabbbbcccc" will return an array with 3 elements - "aaaa", "bbbb", "cccc")
    //  This function is used internally by the format and parseDate routines.
    //    Usage:  o2s.getTokens("string");
    //
    //  Returns an array of strings.
    // ***********************************************
    getTokens :   function (str) {
        var tokens = [];
        var curtoken = "";
        var curchar = "";
        var lastchar = "";
        var idx = 0;
        
        while (idx < str.length) {
          curchar = str.charAt(idx);
          curtoken = "";
          while (str.charAt(idx) == curchar && idx < str.length) {
            curtoken += str.charAt(idx++);
          };
          tokens.push(curtoken);
        };
        return tokens;
    },
    
    //********************************
    // DATE UTILITIES
    //********************************
    
    // ***********************************************
    //   DATE DIFFERENCE
    //  
    //  Finds the difference between two dates, in a specified unit.
    //    Usage:  o2s.dateDiff("unit",startdate, enddate);
    //
    //    Units:  ms  - milliseconds      d - days
    //            s   - seconds           w - weeks
    //            i   - minutes           y - years
    //            h   - hours
    //
    //  Returns a numeric value, or null if the process cannot complete
    // ***********************************************
    dateDiff : function (unit, start, end) {
      start = this.parseDate(start);
      end = this.parseDate(end);
    
      if (!start || !end) { return null; }
      
      var diff = end.getTime() - start.getTime();
      switch (unit.toLowerCase()) {
        case "ms"     : { diff = diff; break; };
        case "s"      : { diff = diff / this.second; break; };
        case "i"      : { diff = diff / this.minute; break; };
        case "h"      : { diff = diff / this.hour; break; };
        case "d"      : { diff = diff / this.day; break; };
        case "w"      : { diff = diff / this.week; break; };
        case "y"      : { diff = diff / this.year; break; };
      };
      return diff;
    },
    
    // ***********************************************
    //   DATE ADD
    //  
    //  Adds a specified amout of the specified unit to a date.
    //    Usage:  o2s.dateAdd("unit", amount, base_date);
    //
    //    Units:  ms  - milliseconds      d - days
    //            s   - seconds           w - weeks
    //            i   - minutes           y - years
    //            h   - hours
    //
    //  Returns a new date object.
    // ***********************************************
    dateAdd   : function (unit, amt, date) {
      var diff = 0;
      switch (unit.toLowerCase()) {
        case "ms"   : { diff = amt * this.millisecond; break;};
        case "s"    : { diff = amt * this.second; break;};
        case "i"    : { diff = amt * this.minute; break;};
        case "h"    : { diff = amt * this.hour; break;};
        case "d"    : { diff = amt * this.day; break;};
        case "w"    : { diff = amt * this.week; break;};
        case "y"    : { diff = amt * this.year; break;};
        default     : { return 0; };
      };
      var date = o2s.parseDate(date);
      var mills = (date) ? date.getTime() + diff: diff;
      var nd = new Date();
      nd.setTime(mills);
      return nd;
    },
    
    // ***********************************************
    //  Formatting Functions
    //  
    //  These functions return a string with the corresponding date element.
    //  These are internal functions
    //
    //  Returns a string
    // ***********************************************
    d         :   function (date) { return date.getDate().toString(); },
    dd        :   function (date) { return this.pad(this.d(date)); },
    ddd       :   function (date) { return this.dayshort[date.getDay()]; },
    dddd      :   function (date) { return this.daylong[date.getDay()]; },
    j         :   function (date) { var y = date.getFullYear(); var ds = new Date(y, 0, 0); return Math.round(this.dateDiff("d", ds, date))  ; },   //jullian date
    m         :   function (date) { return date.getMonth() + 1; },
    mm        :   function (date) { return this.pad(date.getMonth() + 1); },
    mmm       :   function (date) { return this.monthshort[date.getMonth()]; },
    mmmm      :   function (date) { return this.monthlong[date.getMonth()]; },
    y         :   function (date) { return parseInt(date.getFullYear().toString().substr(2)); },
    yy        :   function (date) { return this.pad(this.y(date)); },
    yyyy      :   function (date) { return date.getFullYear(); },
    h         :   function (date) { return (date.getHours()>12?date.getHours()-12:date.getHours()); },
    hh        :   function (date) { return this.pad(this.h(date)); },
    H         :   function (date) { return date.getHours(); },
    HH        :   function (date) { return this.pad(this.H(date)); },
    i         :   function (date) { return date.getMinutes(); },
    ii        :   function (date) { return this.pad(this.i(date)); },
    s         :   function (date) { return date.getSeconds(); },
    ss        :   function (date) { return this.pad(this.s(date)); },
    a         :   function (date) { return (this.H(date)<12?"am":"pm"); },
    A         :   function (date) { return (this.H(date)<12?"AM":"PM"); },
    O         :   function (date) { var tz = (date.getTimezoneOffset() /60) * -1; return (tz<0?tz:"+"+tz); },
    short     :   function (date) { return this.m(date) + "/" + this.d(date) + "/" + this.yy(date); },
    medium    :   function (date) { return this.mmm(date) + " " + this.d(date) + ", " + this.yyyy(date); },
    long      :   function (date) { return this.mmmm(date) + " " + this.d(date) + ", " + this.yyyy(date); },
    shorttime :   function (date) { return this.short(date) + " " + this.HH(date) + ":" + this.ii(date); },
    mediumtime:   function (date) { return this.medium(date) + " " + this.HH(date) + ":" + this.ii(date); },
    longtime  :   function (date) { return this.long(date) + " " + this.hh(date) + ":" + this.ii(date) + ":" + this.ss(date) + " " + this.A(date); },
    iso8610   :   function (date) { return this.yyyy(date) + "-" + this.mm(date) + "-" + this.dd(date) + "T" + this.HH(date) + this.ii(date) + ":" + this.ss(date) + this.O(date); },
    yyyymmdd  :   function (date) { return this.yyyy(date) + "-" + this.mm(date) + "-" + this.dd(date); },
    
    
    // ***********************************************
    //   FORMAT
    //  
    //  Formats a date to an arbitrary format.
    //    Usage:  o2s.format(base_date, formatstring);
    //
    //    Example: o2s.format(new Date("1 Jan 2007", "dd.mmm.yyyy [HH:ii:ss]"));
    //                =>  "01.Jan.2007 [00:00:00]"
    //
    //    Any character in the string not in the following list is treated as a literal and placed into the output string directly.
    //    Available formatting options:
    //
    //      d     - day of month (not padded)       h     - hour, 12 hour clock (not padded)
    //      dd    - day of month (zero padded)      hh    - hour, 12 hour clock (zero padded)
    //      ddd   - day name, abbreviated           H     - hour, 24 hour clock (not padded)
    //      dddd  - day name, full                  HH    - hour, 24 hour clock (zero padded)
    //      m     - month number (not padded)       i     - minutes (not padded)
    //      mm    - month number (zero padded)      ii    - minutes (zero padded)
    //      mmm   - month name, abbreviated         s     - seconds (not padded)
    //      mmmm  - month name, full                ss    - seconds (zero padded)
    //      y     - two digit year (not padded)     a     - am / pm (lowercase)
    //      yy    - two digit year (zero padded)    A     - AM / PM (uppercase)
    //      yyyy  - four digit year                 O     - timze zone offset from GMT.  (that is a letter OH, not number zero)
    //      j     - ordinal day/julian date
    //      jjj   - ordinal day/julian date (same as "j")
    //      
    //    prebuilt formats
    //      short       - "m/d/yy"              
    //      medium      - "mmm d yyyy"          
    //      long        - "mmmm d yyyy"         
    //      shorttime   - "m/d/yy HH:ii"        
    //      mediumtime  - "mmm d yyyy HH:ii"    
    //      longtime    - "mmmm d yyyy hh:ii:ss A O"
    //      iso8610     - "yyyy-mm-ddTHH:ii:ss O"
    //      yyyymmdd    - "yyyy-mm-dd"
    //
    //  Returns a string
    // ***********************************************
    //format a date to an arbtrary format
    format    :   function (date, format) {
      if (!format) { format = "medium"; };   //make sure we have a format
      var output = "";

      if (!o2s.isDate(date)) { return date; };   //exit leaving the date item unchanged
      
      //start with the named formats
      switch (format) {
        case "short"      : { output += this.short(date); break;};
        case "medium"     : { output += this.medium(date); break;};
        case "long"       : { output += this.long(date); break;};
        case "shorttime"  : { output += this.shorttime(date); break;};
        case "mediumtime" : { output += this.mediumtime(date); break;};
        case "longtime"   : { output += this.longtime(date); break;};
        case "iso8610"    : { output += this.iso8610(date); break;};
        case "yyyymmdd"   : { output += this.yyyymmdd(date); break;};
        default           : {
          var tokens = this.getTokens(format);

          for (var x=0; x < tokens.length; x++) {
            var part = "";
            switch (tokens[x]) {
              case "d"          : { part += this.d(date); break;};
              case "dd"         : { part += this.dd(date); break;};
              case "ddd"        : { part += this.ddd(date); break;};
              case "dddd"       : { part += this.dddd(date); break;};
              case "m"          : { part += this.m(date); break;};
              case "mm"         : { part += this.mm(date); break;};
              case "mmm"        : { part += this.mmm(date); break;};
              case "mmmm"       : { part += this.mmmm(date); break;};
              case "y"          : { part += this.y(date); break;};
              case "yy"         : { part += this.yy(date); break;};
              case "yyyy"       : { part += this.yyyy(date); break;};
              case "h"          : { part += this.h(date); break;};
              case "hh"         : { part += this.hh(date); break;};
              case "H"          : { part += this.H(date); break;};
              case "HH"         : { part += this.H(date); break;};
              case "i"          : { part += this.i(date); break;};
              case "ii"         : { part += this.ii(date); break;};
              case "s"          : { part += this.s(date); break;};
              case "ss"         : { part += this.ss(date); break;};
              case "a"          : { part += this.a(date); break;};
              case "A"          : { part += this.A(date); break;};
              case "O"          : { part += this.O(date); break;};
              case "j": case "jjj": { part += this.j(date); break; };
              default           : { part += tokens[x]; break; };
        
            };
            output += part;
//            alert("token" + tokens[x] + "\noutput: " + output + "\npart: " + part);

          };
          break;
        }
      };
      return output;
    },
    
    // ***********************************************
    //  PARSE DATE
    //  
    //  This function attempts to get a date object from a string in an arbitrary format.
    //    Usage:  o2s.parseDate(datestring, formatstring);
    //  
    //    If the format string is omitted, the routine will attempt to find a date using
    //    common formats (in the commonformats[] array.
    //
    //    Format string takes the same arguments as the format routine above.
    //
    //    NOTE: a date string could match more than one format.  In this case, the first format
    //          found that works will be used.  If desired behaviour is not happening, 
    //          specify a format string to force the desired format.
    //
    //  Returns a date object if successful.  Otherwise returns null.
    // ***********************************************
    // Parse a date string if possible.
    // Based on Matt Kruse's parseString function in his date libarary:
    // http://www.mattkruse.com/javascript/date/
    // ***********************************************
    parseDate : function (val, format) {
      if (!val) { return null; }
      val = val.toString();
      
      //if format is not specified, create an array of general formats to try
      if (!format) {
        for (var i=0; i < this.commonformats.length; i++) {
          var d = this.parseDate(val, this.commonformats[i]);
          if (d != null) { 
            return d; 
          };
        };
        return null;
      };
      
      //subfunction to make sure a given value is an integer
      this.isInteger = function(val) {
        for (var i=0; i < val.length; i++) {
          if ("1234567890".indexOf(val.charAt(i))==-1) { 
            return false; 
          };
        };
        return true;
      };
      
      //subfunction to find a subset of the value, based on a format's limits (i.e. day of month cannot be more than two chars)
      this.getInt = function(str,i,minlength,maxlength) {
        for (var x=maxlength; x>=minlength; x--) {
          var token=str.substring(i,i+x);
          if (token.length < minlength) { 
            return null; 
          };
          if (this.isInteger(token)) { 
            return token; 
          };
        };
        return null;
      };
      
      //variables we'll need:
      var day = 0;
      var month = 0;
      var year = 0;
      var hour = 0;
      var minute = 0;
      var second = 0;
      var ampm;
      var min, max;
      var jullian = false;      //cheap out for the finaly touch ups.
      
      //tokenize the format so we can use it later:
      var tokens = this.getTokens(format);

      //check each token and get the corrsponding value, if possible
      idx=0;
      for (var x = 0; x < tokens.length; x++) {
        token = tokens[x];
        switch (token) {
          //numeric day
          case "d": case "dd": {
            day = this.getInt(val, idx, token.length, 2);
            
            if (day < 1 || day > 31 || day == null) { return null; }
            idx += day.length;
            break;
          };
          //text day
          case "ddd": case "dddd": {
            var names = (token == "ddd" ? this.dayshort : this.daylong);
            for (var check=0; check < names.length; check++) {
              if (val.substr(idx, names[check].length).toLowerCase() == names[check].toLowerCase()) {
                day = check;
                idx += names[check].length;
                break;
              };
            }; 
            break;
          };
          
          //jullian/ordinal day
          case "j": case "jjj": {
            var day = this.getInt(val, idx, token.length, 3);
            if (day < 1 || day > 366 || day == null) { return null; };
            idx += day.length;
            jullian = true;
            break;
          };
          
          //numeric month
          case "m": case "mm": {
            month = this.getInt(val, idx, token.length, 2);
            if (month < 1 || month > 12 || month == null) { return null; };
            idx += month.length;
            month = parseInt(month) - 1;
            break;
          };
          
          //text day
          case "mmm": case "mmmm": {
            var names = (token == "mmm") ? this.monthshort : this.monthlong;
            for (var check=0; check < names.length; check++) {
              if (val.substr(idx, names[check].length).toLowerCase() == names[check].toLowerCase()) {
                month = check;
                idx += names[check].length;
                break;
              };
            };
            break;
          };
          
          //years
          case "yyyy": case "yy": case "y": {
            max = 4;
            min = token.length;
            if (token == "y") { min = 2; max = 4; }
            year = this.getInt(val, idx, min, max);
            if (year == null) { return null; }
            idx += year.length;
            if (year.length == 2) {
              if (year > 70) { year = 1900 + parseInt(year); } else { year = 2000 + parseInt(year); }
            };
            break;
          };
          
          //hours
          case "HH": case "H": case "hh": case "h": {
            hour = this.getInt(val, idx, token.length, 2);
            var maxhour = 12;
            if (token == "HH" || token == "H") { maxhour = 23; };
            if (hour < 0 || hour > maxhour || hour == null) { return null; };
            idx += hour.length;
            break;
          };
          
          //minutes and seconds
          case "i": case "ii": case "s": case "ss": {
            minute = this.getInt(val, idx, token.length, 2);
            if (minute < 0 || minute > 59 || minute == null) { return null; };
            idx += minute.length;
            break;
          };
          
          //AM or PM
          case "a": case "A": {
            var ampm = val.substr(idx, 2).toUpperCase();
            if (ampm == "AM" || ampm == "PM") {
              idx += ampm.length;
            }
            else { 
              return null;
            };
            break;
          }
          
          default: {
            var check = val.substr(idx, token.length);
            if (check != token) { return null; }
            idx += token.length;
          };
        };
      };
    
      //final checks.
      // 1. make sure the day matches the number of days in the month
      if (!jullian) {
        if (month == 2) {
          if ( ( (year%4==0) && (year%100 != 0) ) || (year%400==0) ) { // leap year
            if (day > 29) { return null; };
          };
        }
        else {
          if (day < 1 || day > this.monthdays[month]) { return null; };
        };
      };
      // 2. make sure the hour reflects the am/pm setting
      if (parseInt(hour) < 12 && ampm == "PM") {
        hour = parseInt(hour) + 12;
      }
      else if (parseInt(hour) > 11 && ampm == "AM") {
        hour = parseInt(hour) - 12;
      };
      return new Date(year, month, day, hour, minute, second);
    },

    // ***********************************************
    //   IS DATE
    //  
    //  this method indicats if the item is a valid date.
    //    usage:    o2s.isDate(datestring, format);
    //
    //          datestring can be a date object, or a text string.
    //          format is optional, but can be used to explicitly indicate the desired format.
      //          See the format method above for formatting options.
    //
    //  Returns a true or false value
    // ***********************************************
    isDate : function (date, format) {
      d = o2s.parseDate(date, format);
      if (d != null) { return true; } else { return false; };
    },


    // ***********************************************
    //   DATE PICKER CALENDAR
    //  
    //  this method creates the calendar for the date picker.
    //  It should not be called directly.  Instead the showPopup() method should be used.
    //
    //  Returns a string
    // ***********************************************
    //  based on code found at http://jszen.blogspot.com/2007/03/how-to-build-simple-calendar-with.html
    // ***********************************************
    calendar : function (data) {
      showDate = ((!data || data.date == null) ? new Date() : data.date);
      //showDate is to be a date object.
      var showDate = (isNaN(showDate) || showDate == null) ? new Date() : showDate;
      var showYear = showDate.getFullYear();
      var showMonth = showDate.getMonth();
      var firstday = new Date(showYear, showMonth, 1).getDay();      
      var monthLength = this.monthdays[showMonth];
      //check for leapyear
      if (showMonth == 1) { // February only!
        if ((showYear % 4 == 0 && showYear % 100 != 0) || showYear % 400 == 0){
          monthLength = 29;
        };
      };
      
      var html = "<table class=\"" + this.classCal + "\" id=\"" + this.medium( showDate)+ "\">";
      html += "<tr>";
      html += "<th>&nbsp;</th>";
      html += "<th colspan=\"5\">";
      html += this.monthshort[showMonth] + "&nbsp;" + showYear;
      html += "</th>";
      html += "<th><a href=\"#\" id=\"o2sCalClose\">X</a></th>";
      html += "</tr>";
      html += "<tr>";
      html += "<td><a id=\"o2sCalPriorYear\" href=\"#\">" + this.popupTextLastYear + "</a></td>";
      html += "<td><a id=\"o2sCalPriorMonth\" href=\"#\">" + this.popupTextLastMonth + "</a></td>";
      html += "<td colspan=\"3\" style=\"text-align: center;\"><a href=\"#\" id=\"o2sCalToday\">" + this.popupTextToday + "</a></td>";
      html += "<td style=\"text-align: right;\"><a id=\"o2sCalNextMonth\" href=\"#\">" + this.popupTextNextMonth + "</a></td>";
      html += "<td style=\"text-align: right;\"><a id=\"o2sCalNextYear\" href=\"#\">" + this.popupTextNextYear + "</a></td>";
      html += "</tr>";
      html += "<tr class=\"" + this.classCalHeader + "\">";
      for (var i=0; i < this.dayshort.length; i++) {
        html += "<td class=\"" + this.classCalHeaderDay + "\">";
        html += this.dayletter[i];
        html += "</td>";
      };
      html += "</tr>";
      var curday = 1;
      while (curday <= monthLength) {
        html += "<tr>";
        for (var i=0; i < 7; i++) {
          if ((curday <= 1 && i < firstday) || curday > monthLength) {
            html += "<td class=\"" + this.classCalDay + "\" id=\"\">";
            html += "&nbsp;";
          }
          else {
            var dayclass = ((curday == showDate.getDate()) ? this.classCalDayTarget : this.classCalDay);
            html += "<td class=\"" + dayclass + "\" id=\"" + this.medium( new Date(showYear, showMonth, curday) ) + "\">";
            html += curday;
            curday++;
          };
          html += "</td>";
        };
        html += "</tr>";
      };
      if (o2sCalShowTime) {
        html += "<tr>"
        html += "<td>&nbsp;</td>";
        html += "<td colspan=\"2\" style=\"text-align:right;\"><select id=\"o2sCalTimeHour\">";
        for (var i=0; i < 24; i++) { 
          html += "<option value=\"" + this.pad(i) + "\">" + this.pad(i) + "</option>";
        };
        html += "</select> : </td>";
        html += "<td colspan=\"2\"><select id=\"o2sCalTimeHour\">";
        for (var i=0; i < 60; i++) { 
          html += "<option value=\"" + this.pad(i) + "\">" + this.pad(i) + "</option>";
        };
        html += "</select></td>";
        html += "<td>&nbsp;</td>";
        html += "</tr>";
      };
      html += "</table>";
      
      return html;
    },
    
    // ***********************************************
    //   CALENDAR CLOSE
    //  
    //  this method handles when the calendar should be closed.
    //  It should not be called directly.
    //
    // ***********************************************
    calendarClose : function () {
      if (o2sCalObject && $("#o2sCalendar").css("display") == "block") {
        $("#o2sCalendar").slideUp("fast", function () { $("#o2sCalendar").remove(); });
        o2sCalObject = false;
        return false;
      };
    },
    
    // ***********************************************
    //   CALENDAR EVENTS
    //  
    //  this method sets up the event handlers for the date picker calendar
    //  It should not be called directly.  
    //
    // ***********************************************
    calendarEvents : function (data) {
      if (!data) { data = {}; };
      if (!data.format) { data.format = "medium"; };
    
      //Assign event handlers
      //the close link
      $("#o2sCalClose").click(o2s.calendarClose);
      
      //handle if a click happens outside the calendar
//unstable
//      $(":not(#o2sCalendar)").click(o2s.calendarClose);

      //hover effects
      $("." + this.classCalDay).hover(
          function () {$(this).addClass(o2s.classCalHover); },
          function () { $(this).removeClass(o2s.classCalHover); }
      );
      

      //handle when day is clicked
      $("." + this.classCalDay).click( function() {
        if ($.trim($(this).text()).length > 0) {
          var d = new Date($(this).attr("id"));
          $(o2sCalObject).val(o2s.format(d, data.format));
          o2s.calendarClose();    //close the calendar
          return false;
        };
      });
      $("." + this.classCalDayTarget).click( function() {
        if ($.trim($(this).text()).length > 0) {
          var d = new Date($(this).attr("id"));
          $(o2sCalObject).val(o2s.format(d));
          $("#o2sCalClose").click();    //close the calendar
          return false;
        };
      });
      
      //Handle the year/month/today navigation links
      $("#o2sCalPriorYear").click( function () {
        var d = new Date ( $("." + o2s.classCal).attr("id") );
        d.setYear(d.getFullYear() - 1);
        $("#o2sCalendar").empty().append(o2s.calendar({date: d})); 
        o2s.calendarEvents(data);
        return false;
      });
      $("#o2sCalPriorMonth").click( function () { 
        var d = new Date ( $("." + o2s.classCal).attr("id") );
        d.setMonth(d.getMonth() - 1);
        $("#o2sCalendar").empty().append(o2s.calendar({date: d})); 
        o2s.calendarEvents(data);
        return false;
      });
      $("#o2sCalNextYear").click( function () {
        var d = new Date ( $("." + o2s.classCal).attr("id") );
        d.setYear(d.getFullYear() + 1);
        $("#o2sCalendar").empty().append(o2s.calendar({date: d})); 
        o2s.calendarEvents(data);
        return false;
      });
      $("#o2sCalNextMonth").click( function () { 
        var d = new Date ( $("." + o2s.classCal).attr("id") );
        d.setMonth(d.getMonth() + 1);
        $("#o2sCalendar").empty().append(o2s.calendar({date: d})); 
        o2s.calendarEvents(data);
        return false;
      });
      $("#o2sCalToday").click( function () {
        $("#o2sCalendar").empty().append(o2s.calendar({date: new Date()})); 
        o2s.calendarEvents(data);
        return false;
      });
    
    },
    
    // ***********************************************
    //   SHOW POPUP
    //  
    //  this method causes the date picker to become visible, and positioned properly.
    //    usage:    o2s.showPopup{options, object};
    //
    //      - object is the objec the date picker should be associated with and feed it's resulting date to.
    //      - options is an object that helps define how the date picker functions:
    //          options.date    - the default date to set as the current date in the calendar.
    //          options.focus   - true/false value - if true, the calendar will open when the object receives focus.
    //                            Otherwise, it will open when the object is clicked.
    //          options.parent  - The object to set the date value into.
    //                            Useful when the object to initiate the date picker is not a text field.
    //                            (i.e. activation icon, button, text, link, etc.)
    //
    // ***********************************************
    showPopup : function(data, obj) {
      if (!data) { data = {}; };
      if (data && data.parent) { obj = data.parent; };   //see if we need to override the object
    
      //abort if we are already showing the popup for the requested object
      if (obj == o2sCalObject) { return; };

      //now set the o2sCalObject  (global object to help with position/values)
      o2sCalObject = obj;
      
      var d = this.parseDate($(obj).val());
      if (d) { data.date = d; }
      $("body").append("<div id=\"o2sCalendar\" style=\"display: none;\">" + this.calendar(data) + "</div>");
      //make sure calendar is not already visible
      if ($("#o2sCalendar").css("display") == "block") { $("#o2sCalendar").hide(); }
      
      //position the calendar under the element in question
      $("#o2sCalendar").css({position: "absolute", top: obj.offsetTop + obj.offsetHeight, left: obj.offsetLeft });
      
      //show it
      $("#o2sCalendar").slideToggle("fast", function () {
        if ($(this).css("display") == "none") { $(this).remove(); }
      });
      
      //apply the event handlers
      this.calendarEvents(data);
    },

};


//*************************************
// jQuery wrapper functions to make life easy
//*************************************
jQuery.fn.datePicker = function (opts) {
  return this.each( function () {
      $(this).focus( function () { o2s.showPopup(opts,this); } );
  });  
};

jQuery.fn.dateDiff = function (unit, target) {
    var d = o2s.dateDiff(unit, $(this).val(), target);
    return d;    
};

jQuery.fn.dateAdd = function (unit, amt) {
    var d = o2s.dateAdd(unit, amt, $(this).val());
    return d;
};

jQuery.fn.dateFormat = function (format) {
  return this.each( function () {
    var d = o2s.parseDate($(this).val());
    $(this).val(o2s.format(d, format));
  });
};

jQuery.fn.parseDate = function (format) {
    var d = o2s.parseDate($(this).val());
    return d
};

jQuery.fn.isDate = function (format) {
    return o2s.isDate($(this).val(), format);
};











