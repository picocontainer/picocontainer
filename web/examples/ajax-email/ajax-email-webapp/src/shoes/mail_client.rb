require 'rubygems'
require 'erb'
require 'net/http'  
require 'enumerator'  
require 'ping'
require 'time'

require 'email_remote'

class Navigation
  def initialize(slot)
    @view = slot  
    render
  end
  
  def update_inbox_count(count)
    @view.app{
      @inbox_label.replace "Inbox #{count}" 
    }
  end   
  
  def update_total_mail_count(count)
    @view.app{
       @mail_label.replace "Mail #{count}" 
    }
  end
  
  private
  def render
    @view.append {  
      @view.app {  
        stack :margin_left => 5, :margin_right => 5 do
          stack do  
            @mail_label = para 'Mail 0', :stroke => '#222', :size => 'x-small'
          end

          stack do  
            background white
            border red , :curve => 1
            @inbox_label = para 'Inbox 0', :stroke => '#222', :size => 'x-small'
          end

          stack do  
            @sent_label = para 'Sent 0', :stroke => '#222', :size => 'x-small'
          end  
        end
      }
    }
  end
end

class InboxView
  def initialize(slot)
    @view = slot
    @message_view = {}
    @messages = {}
  end   
  
  def slot
    @view
  end 
  
  attr_accessor :message_view, :messages
  
  def show_snippet(message)  
    view = @message_view[message[:id]][:summary]   
    @view.app do 
     # Shoes.p "snippet view is #{view}"    
     view.clear
     
     view.background "#fff".."#eed"  
     hi = view.background "#ddd".."#ba9", :hidden => true
     
     elem = message[:read] ? message[:subject] : strong(message[:subject])   
     time = Time.parse(message[:sentTime]).strftime("%b %d %I:%m %p ")
     @inbox_view.render(message[:from], message[:to], link(elem) ,time,  view) 
     
     view.hover { hi.show }
     view.leave { hi.hide }
     view.click {  @inbox_view.show_message(message) } 
     view.show      
    end
  end
  
  def show_message(message)
    view = @message_view[message[:id]][:full_text]   
    summary_view = @message_view[message[:id]][:summary] 
    
    @view.app do  
     view.show  
     summary_view.hide 
     
     message = read_message(message[:id]) 
     view.clear
     view.background '#eef'..'#ffb'
     
     @inbox_view.render_message_body(message, view) 
    end
  end
  
  def hide_message(message)
    @view.app {  
      @inbox_view.message_view[message[:id]][:full_text].hide  
      @inbox_view.show_snippet(message)
    } 
  end 
  
  def render(from, to, subject,date, slot) 
    slot.append {
      @view.app { 
        flow(:width => 1.0) do
          flow (:width => 0.20) {  para from }
          flow (:width => 0.15) {  para to }
          flow (:width => 0.45) {  para subject }
          flow (:width => 0.20) {  para date }
        end
      }
    }
  end
    
  def render_message_body(message, slot)
    slot.append {
      @view.app {
         # background '#eef'..'#ffd'  
         flow :width => 1.0 do
          flow :width => 0.7 do
            para "From  #{message[:from]}  to you", :align => 'left'
          end
          flow :width => 0.2 do
            para time = Time.parse(message[:sentTime]).strftime("%b %d %I:%m %p ") , :align => 'right'
          end  
          flow :width => 0.1 do   
            button('x') { @inbox_view.hide_message(message) }  
          end
         end
         
         para "Subject: #{message[:subject]}", :align => 'left'
         para message[:message], :align => 'left'
      }
    }
  end
  
  def add_messages(messages)    
    @view.app { 
     stack = @inbox_view.slot.stack :width => 1.0
     stack.background gray
     @inbox_view.render('From', 'To','Subject', 'Date', stack)
    }
    
    messages.each_with_index do |message,index| 
      @messages[message[:id]] = message   
      @message_view[message[:id]] = {}
      @view.append { 
        @view.app do 
          style(Link, :stroke => black, :underline => nil, :weight => "strong")
          style(LinkHover, :stroke => black, :fill => nil, :underline => nil)
        
          @inbox_view.message_view[message[:id]][:summary] = stack :width => 1.0
          @inbox_view.message_view[message[:id]][:full_text] = stack :width => 1.0
          
          @inbox_view.show_snippet(message) 
        end
      }   
    end
  end  
  
end             


Shoes.app :title => "Mail client", :width => 1000, :height => 700 do
  
  @inbox_view = nil  
  @navigation = nil
  @status = nil   
  @title = nil 
  @logged_in  = false
    
	  
  def logIn(name , password)
    Shoes.p "logging in #{name}.. #{password}"  

    @auth = Auth.new(Connection.new('ph-jdo.appspot.com', 80, '/ruby', RubyParser.new))
    result = @auth.login(name, password)      
    if(result)
      @logged_in = true 
      # Shoes.p "logged in #{@logged_in}"  
      "Success"
    else               
      @logged_in = false  
      # Shoes.p "logged in #{@logged_in}"
      "Wrong user name or password"
    end   
  end   

  def load_messages  
    if(@logged_in)                 
      @status.replace 'loading messages'
      inbox_messages =  @auth.inbox.messages
      @inbox_view.add_messages(inbox_messages) 
      update_count
     else
       @title.replace 'Not logged in'
    end
  end    
  
  def update_count
     inbox_messages = @auth.inbox.messages
     @navigation.update_total_mail_count inbox_messages.length
     @navigation.update_inbox_count inbox_messages.reject{|m| m[:read] == true}.length
  end
  
  def read_message(id) 
    message = @auth.inbox.read(id)  
    update_count  
    @status.replace "displaying message with id #{id}"
    message
  end
  
  @main_window  = stack :margin => 10, :margin_top => 10, :hidden => true do   
     background "#C7EAFB"  

     flow :margin_right => 1 do  
       border "#00D0FF", :strokewidth => 3, :curve => 5
       stack :margin_left => 1, :margin_right => 1, :margin_top => 5, :width => 1.0 do
          elem = flow do
            @title = para "Mail for" , :stroke => black  
            @status = inscription  '',  :stroke => gray, :align => 'right' , :size => 'small'  
          end      
          stack :width => 1.0 do
              border  black
           end
       end    

       flow :width => 1.0, :margin_bottom => 5 do
         flow :margin_left => 1, :width => "19%" do
            @navigation = Navigation.new(stack)
         end  

          flow :width => '1%' do
            background black
          end   

          view = flow :margin_right => 1, :width => "80%" do
              @status.replace 'loading messages'  
              sleep 0.5
          end

          @inbox_view  = InboxView.new(view)
       end 
     end  
 	end 
 	
 	@login_window = stack do
    caption 'Log in please'
    message = inscription '', :stroke => red
    para 'user name'
    name = edit_line  'Gill Bates'
    
    para 'password'
    password = edit_line   '1234'
    
    button "log In" do
      # Shoes.p [name.text, password.text]
      result = logIn(name.text,password.text)  
      if(@logged_in)
        @login_window.hide
        @main_window.show 
        @title.text += " " + name.text
        load_messages  
      else
        message.replace result
      end
    end
  end  
end  


