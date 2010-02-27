#--
# Copyright (C) Swiby Committers. All rights reserved.
# 
# The software in this package is published under the terms of the BSD
# style license a copy of which has been included with this distribution in
# the LICENSE.txt file.
#
#++

require 'time'

require 'pretty_time'

require 'email_views'

class LoginController

  def initialize connection
    @connection = connection
    @error_message = ''
  end
  
  def login= login
    @login = login
  end
  
  def password= password
    @password = password
  end
  
  def ok
    
    auth = Auth.new(@connection)
    
    unless auth.logIn(@login, @password)
      @error_message = "<html><font color='red'>#{@connection.last_error[:message]}</font>"
    else
      Views[:mailbox_view].instantiate(MailboxController.new(@connection))
      @window.close
    end
    
  end
  
  def exit
    @window.exit
  end
  
  def error
    @error_message
  end
  
end

class MailboxController
  
  INBOX = 0
  SENTBOX = 1
  
  def initialize connection
    @connection = connection
    @box_index = INBOX
    @box = Inbox.new(@connection)
  end
  
  def current_mailbox= box
    
    return if @box_index == box
    
    @current = nil
    @messages = nil 
    
    @box_index = box
    
    if box == INBOX
      @box = Inbox.new(@connection)
    else
      @box = Sent.new(@connection)
    end
      
  end
  
  def current_mailbox_content= index
    @current = index
  end
  
  def mailbox_content_changed?
    @messages.nil?
  end
  
  def mailbox_content
    @messages = format_messages(@box.messages)
  end
  
  def detail
    
    if @current
      message = @messages[@current]
      "From: #{message[:from]}\nSubject: #{message[:subject]}\n\n#{message[:message]}"
    end
  
  end

  def reply
    
    message = @messages[@current]
    
    subject = "Re: #{message[:subject]}"
    body = "\n====================\n#{message[:message]}"
    
    Views[:mail_composer].instantiate(MailComposerController.new(Sent.new(@connection), message[:from], subject, body))
    
  end
  
  def may_reply?
    @box_index == INBOX and not @current.nil?
  end
  
  def new_mail
    Views[:mail_composer].instantiate(MailComposerController.new(Sent.new(@connection)))
  end
  
  private
  
  def format_messages messages
    
    messages.each do |message|
      message[:sentTime] = format_pretty_time(message[:sentTime])
    end
    
    messages
    
  end
  
end

class MailComposerController
  
  attr_accessor :recipient, :subject, :body
  
  def initialize sent_box, recipient = nil, subject = nil, body = nil
    @sent_box, @recipient, @subject, @body = sent_box, recipient, subject, body
  end
  
  def cancel
    @window.close
  end
  
  def send_mail
    @sent_box.send @recipient, @subject, @body
    @window.close
  end
  
  def may_send_mail?
    not @recipient.nil? and @recipient.strip.length > 0
  end
  
end

if $0 == __FILE__

  options = parse_options(ARGV, __FILE__)

  Views[:login_view].instantiate(LoginController.new(create_connection(options)))

end