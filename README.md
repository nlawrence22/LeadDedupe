# LeadDedupe

A small utility for de-duplicating JSON lists of leads with a specific
input format.

The utility takes in a file of JSON formatted data, removes duplicates
based on specific rules, logs the changes and then outputs the sanitized
list.  The rules are:

  1. The data from the newest date should be preferred
  2. Duplicate IDs count as dupes. Duplicate emails count as dupes. Other
     duplicate values do not constitute a duplicate record.
  3. If dates are identical, use the record provided last in the list.

The command line program expects one argument and up to a second, optional argument:

  1. The input filepath relative to the working directory of the program
  2. The output filepath relative to the working directory of the program (optional)

The output argument is optional.  If the output filepath is omitted,
output will be written to output.json in the parent directory of the
program.

A log of changes will be written to changes.log in the working directory
of the program

The program will exit if no arguments or more than two arguments are provided, or
if it encounters any issues with file I/O.


# Build

This project requires [Git](https://git-scm.com/), [Java 8](http://openjdk.java.net/),
and [Apache Maven](https://maven.apache.org/).  It was tested on:

* OpenJDK Runtime Environment (build 1.8.0_131-b12) / OpenJDK 64-Bit Server VM (build 25.131-b12, mixed mode)
* Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-03T12:39:06-07:00)
* OS name: "linux", version: "4.11.3-200.fc25.x86_64", arch: "amd64", family: "unix"

To build the project, first [clone the repository](https://help.github.com/articles/cloning-a-repository/)
on your local machine.  Once the repository is cloned, you should be able to
build using the command `mvn clean package`.  This will run tests then
generate a jar at the location `<repo root>/target/leadDedupe-[version].jar`

# Usage

```
Usage:
java -jar leadDedupe-[version].jar input_filename [output_filename]
```

To use the created jar, it's best to move it to its own directory.

`mv <repo root>/target/leadDedupe-[version].jar /some/other/directory`

Once the jar is in it's own location, you need to copy your leads file
to the same directory as the jar, or a subdirectory thereof.

`mv /some/leads/file/leads.json /path/to/program`

Your directory should now look something like this:

```
[nlawrence@localhost programs]$ ll
-rw-rw-r--. 1 nlawrence nlawrence 1873777 Jun  9 08:24 leadDedupe-0.1.0.jar
-rw-rw-r--. 1 nlawrence nlawrence    1726 Jun  9 08:26 leads.json
```

Now execute the jar by running:
`java -jar leadDedupe-0.1.0.jar leads.json`

An output file will be created at `output.json` and a changelog at `changes.log`
in that same directory!

# License

This project is licensed under the Terms of the Eclipse Public License version 1.0.
You may find all license terms in the [LICENSE](LICENSE) file.

This project also makes use of software under the following licenses:

[Eclipse Public License, - v 1.0](http://www.eclipse.org/legal/epl-v10.html)

  * [JUnit](http://junit.org)
  * [System Rules](https://stefanbirkner.github.io/system-rules/) by Stefan Birkner

[Apache License, version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
  * [Log4J2](https://logging.apache.org/log4j/2.x/)
