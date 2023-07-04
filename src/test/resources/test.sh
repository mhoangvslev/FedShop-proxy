#!/bin/bash

# Execute JMeter test plan "Test500Users.jmx" in non-GUI mode and save the results to "log.jtl"
./apache-jmeter-5.6/bin/jmeter -n -t Test500Users.jmx -l log.jtl -e -o results/
