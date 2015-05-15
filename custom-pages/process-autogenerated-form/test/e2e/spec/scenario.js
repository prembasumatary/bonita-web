'use strict';
describe('custom page', function() {



  beforeEach(function(){
    browser.get('http://localhost:3000/bonita/process-autogenerated-form/index.html?id=2');
  });

  afterEach(function(){
    browser.manage().logs().get('browser').then(function(browserLog) {
      console.log('log: ' + require('util').inspect(browserLog));
    });
  });

  it('should have a title', function() {
    expect(browser.getTitle()).toEqual('Register new Support Ticket');
  });


  it('should have a autofill button', function() {
    expect($$('#autofill').count()).toBe(1)
    expect(element(by.id('autofill')).getText()).toEqual('Autofill');
  });


  it('should *NOT* have a autofill button', function() {
    // If there is no contract, or contract contains no input, no autofill button should be added to the DOM.
    browser.get('http://localhost:3000/bonita/process-autogenerated-form/index.html?id=4');
    expect($$('#autofill').count()).toBe(0)
  });

});
