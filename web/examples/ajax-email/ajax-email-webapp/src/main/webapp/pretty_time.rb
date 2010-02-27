#--
# Copyright (C) Swiby Committers. All rights reserved.
# 
# The software in this package is published under the terms of the BSD
# style license a copy of which has been included with this distribution in
# the LICENSE.txt file.
#
#++

def format_pretty_time t
  
  diff = Time.now - Time.parse(t)
  
  day_diff = (diff / 86400).floor
  
  case day_diff
    when 0
      if diff < 60
        "just now"
      elsif diff < 120
        "1 minute ago"
      elsif diff < 3600
        "#{( diff / 60 ).floor} minutes ago"
      elsif diff < 7200
        "1 hour ago"
      elsif diff < 86400
        "#{( diff / 3600 ).floor} hours ago"
      end
    when 1
      "Yesterday"
    else
      if day_diff < 7
        "#{day_diff} days ago"
      elsif day_diff < 31
        "#{( day_diff / 7 ).ceil} weeks ago"
      else
        "pretty old"
      end
    end
  
end
  
