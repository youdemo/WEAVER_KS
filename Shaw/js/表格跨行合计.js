//table 合计行如果数值相同，则合并单元格
//注意要增加区分字段，不然遇到所有的相同数字都会进行合并，出错
function autoRowSpanNew(tb, row, col,diff1,diff2) {
			//td:table id;row:第几行;col:第几列(0开始);diff1:区别参数1(第几列);diff2:区别参数2(第几列);
			var lastValue = "";
			var value = "";
			var pos = 1;
			var lastEmpid = "";
			var empid = "";
			var lastLeaveType = "";
			var leaveType = "";
			for (var i = row; i < tb.rows.length; i++) {

				value = tb.rows[i].cells[col].innerText;
				empid = tb.rows[i].cells[diff1].innerText;
				leaveType = tb.rows[i].cells[diff2].innerText;
				//alert("empid="+empid);
				if (lastValue == value&&lastEmpid == empid&&lastLeaveType == leaveType) {
					tb.rows[i].deleteCell(col);
					tb.rows[i - pos].cells[col].rowSpan = tb.rows[i - pos].cells[col].rowSpan + 1;
					pos++;
				} else {
					lastValue = value;
					lastEmpid = empid;
					lastLeaveType = leaveType;
					pos = 1;
				}
			}
		}
