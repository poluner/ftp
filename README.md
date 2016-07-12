这里有两个关键点

关键点1：流的关闭一定要放在finally中，因为要确保在异常发生情况下仍然可以关闭流

关键点2：在主动模式出现错误：Address already in use: connect，
原因是短时间内new Socket操作过多，
而socket.close()操作并不能立即释放绑定的端口，
而是把端口设置为TIME_WAIT状态过段时间(默认240s)才释放，
(用netstat -na可以看到)最后系统资源耗尽，
(windows上是耗尽了pool of ephemeral ports 这段区间在1024-5000之间)，

一种最容易实现但是很耗时的解决办法就是在new Socket操作之前增加TIME_WAIT时间的延迟，确保端口被释放。
