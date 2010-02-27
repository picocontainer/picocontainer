#--
#############################################################################
# Copyright (C) PicoContainer Organization. All rights reserved.            #
# ------------------------------------------------------------------------- #
# The software in this package is published under the terms of the BSD      #
# style license a copy of which has been included with this distribution in #
# the LICENSE.txt file.                                                     #
#                                                                           #
#############################################################################
#++

#
# Scripted::Builder is a domain-specific language for use with JRuby and
# ScriptedPicoContainer for configuring containers and components.
#
module Scripted
  Parameter = org.picocontainer.Parameter
  DefaultScriptedPicoContainer = org.picocontainer.classname.DefaultClassLoadingPicoContainer
  DefaultPicoContainer = org.picocontainer.DefaultPicoContainer
  ComponentParameter = org.picocontainer.parameters.ComponentParameter
  ConstantParameter = org.picocontainer.parameters.ConstantParameter
  JRubyContainerBuilder = org.picocontainer.script.jruby.JRubyContainerBuilder
  ClassPathElementHelper = org.picocontainer.script.util.ClassPathElementHelper
  ComponentElementHelper = org.picocontainer.script.util.ComponentElementHelper  
  include_class 'org.picocontainer.script.util.ContainerElementHelper'

  MARKUP_EXCEPTION_PREFIX = JRubyContainerBuilder::MARKUP_EXCEPTION_PREFIX

  class Key
    def initialize(key = nil)
      @key = key
    end

    def build() ComponentParameter.new(@key) end
  end

  class Constant
    def initialize(val)
      @const = val
    end

    def build() ConstantParameter.new(@const) end
  end
  
  class Zero
      def build()
        org.picocontainer.parameters.DefaultConstructorParameter::INSTANCE 
      end
  end

  class Params
    def initialize(params = [])
      @params = [params].flatten.map do |p|
        p.respond_to?(:build) ? p : Constant.new(p)
      end
    end

    def build
      params = Parameter[@params.length].new
      @params.each_with_index {|p, i| params[i] = p.build }
      params
    end
  end


  class Component
    def initialize(options)
      @key, @klass, @instance, @cnkey = nil, nil, nil, nil
      if options.kind_of?(Hash)
        @instance = options[:instance]
        @key      = options[:key]
        @klass    = options[:class]
        @params   = options[:parameters]
        @cnkey = options[:classNameKey]
      else
        @klass = options
      end
      @instance or @klass or raise "#{MARKUP_EXCEPTION_PREFIX}either :class or :instance required"
    end

    def build(container)

      parms = Params.new(@params).build if @params
      ComponentElementHelper.makeComponent(@cnkey, @key, parms, @klass, container, @instance)

#      args = [@key]
#      if @instance
#        method = :registerComponentInstance
#        args << @instance
#      else
#        method = :registerComponentImplementation
#        args << @klass
#      end
#      args << Params.new(@params).build if @params
#      args.delete(nil)
#      container.send(method, *args)
    end
  end

  class Container
    def initialize(*componentFactories)
      unless componentFactories.last.is_a?(Hash)
        options = {}
        componentFactories << options
      else
        options = componentFactories.last
      end

#      Removed so make child container work  This eventually needs to be
#      put back in
#      @impl     = DefaultScriptedPicoContainer
      @impl = nil      
      @parent   = options[:parent]
      @componentFactory      = options[:component_adapter_factory]
      @monitor  = options[:component_monitor]
      @comps    = []
      @children = []
    end

    def build(&block)
      @container = construct_container
      instance_eval(&block) if block
      @comps.each {|comp| comp.build(@container)}
      @container
    end

    def key(key)
      Key.new(key)
    end

    def constant(val)
      Constant.new(val)
    end
    
    def zero
      Zero.new()
    end
    
    def component(options)
      @comps << Component.new(options)
    end

    def container(*componentFactories, &block)
      unless componentFactories.last.is_a?(Hash)
        options = {}
        componentFactories << options
      else
        options = componentFactories.last
      end

      if !options[:parent].nil? && options[:parent].equal?($parent)
          raise "#{MARKUP_EXCEPTION_PREFIX}You can't explicitly specify a parent in a child element."
      end
      if options[:parent].nil?
        options[:parent] = @container
      end
      container = Container.new(*componentFactories)
      container.build(&block)
      container
    end

    def classPathElement(options = {}, &block)
      cpe = ClassPathElement.new(ClassPathElementHelper.addClassPathElement(options[:path], @container), @container)
      cpe.build(&block)
      cpe
    end

    class ClassPathElement
      def initialize(classPathElement, container)
        @container = container
        @classPathElement = classPathElement
      end
      def build(&block)
        instance_eval(&block) if block
      end
      def grant(options = {})
        @classPathElement.grantPermission(options[:perm])
      end
    end

    private
    def construct_container
      if @parent && !@componentFactory && !@impl
        container = @parent.makeChildContainer
      else
        container = ContainerElementHelper.makeScriptedPicoContainer(@componentFactory, @parent, classloader)
        @parent.addChildContainer(container) if @parent
      end
      container.changeMonitor(@monitor) if @monitor && container.respond_to?(:changeMonitor)
      container
    end

    def classloader
      @parent && @parent.getComponentClassLoader || $parent && $parent.getComponentClassLoader
    end

  end

  module Builder
    def container(options = {}, &block)
      Container.new(options).build(&block)
    end
  end
end

class Object
  include Scripted::Builder
end
