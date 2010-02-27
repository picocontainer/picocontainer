#--
# Copyright (C) Swiby Committers. All rights reserved.
# 
# The software in this package is published under the terms of the BSD
# style license a copy of which has been included with this distribution in
# the LICENSE.txt file.
#
#++

require 'swiby/mvc'
require 'swiby/mvc/text'
require 'swiby/mvc/table'
require 'swiby/mvc/label'
require 'swiby/mvc/combo'
require 'swiby/mvc/button'
require 'swiby/mvc/editor'

require 'swiby/component/form'

Swiby.define_named_view(:login_view) {

  form {
    
    use_styles "styles.rb"
    
    title 'Login'

    width 540
    height 270

    content {

      section "Credentials"
      
        label '', :name => :error
        
        input 'Login', '', :name => :login
        password 'Password', '', :name => :password
        
        label "<html><h3>Demo login</h3>Use <i>'Gil Bates'</i> or <i>'Beeve Salmer'</i> with <i>1234</i> as password<br>"

      next_row
      
        button 'OK', :name => :ok
        button 'Exit', :name => :exit

    }

    visible true

    dispose_on_close
    
  }
  
}

Swiby.define_named_view(:mailbox_view) {

  frame {
  
    use_styles "styles.rb"
    
    title 'Mailbox View' 

    width 700
    height 645

    toolbar {
      combo ['In box', 'Sent box'], :name => :mailbox
      label ' '
      button 'New', :name => :new_mail
      label ' '
      separator
      button 'Exit', :name => :exit
    }
    
    form {

      section "Mailbox", :expand => 40
      
        table :name => :mailbox_content, :columns =>  ['To', 'From', 'Subject', 'Date'], :fields =>  [:to, :from, :subject, :sentTime]

      next_row
      section "Message", :expand => 60
        
        editor :readonly => true, :name => :detail
      
      next_row
      section
      
        button 'Reply', :name => :reply

    }

    visible true

  }
  
}

Swiby.define_named_view(:mail_composer) {

  form {
  
    use_styles "styles.rb"
  
    width 400
    height 250
    
    input 'To', '', :name => :recipient
    input 'Subject', '', :name => :subject
    
    next_row
      editor :name => :body

      button 'Send', :name => :send_mail
      button 'Cancel', :name => :cancel
    
    visible true
    
    dispose_on_close
    
  }
  
}