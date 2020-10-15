# umtrace

## Description: 
UmTrace is a library for tracing the application flow and aggregate the execution time across multiple invocations. UmTrace will create a tree of selected execution path's, aggregate the time spend in these sections of the code and illustrate the hot spots. 

UmTrace works by amending the code with trace statements. 

## Shortcomings: 
Multiple!
* Trace is expected to have a given start and a matching end. If enter and exit's get out of balance strange things happen. 
* There is no build in magic to take care of the execution taking an unexpected path, e.g. Exceptions. 

## Usage: 
UmTrace is expected to be included on the classpath. The source is ammended with trace statements referring to UmTrace. The org.um.umtrace.TestUmTrace.java show some examples. 



## Contributing: 
This is an example, not a project under active development. Feel free to report issues or make comments. Do not expect an answer.  

## Credits: 
* Author: Torbjörn Österdahl (ultra-marine.org)

## License: 
MIT License
